import com.my.*
import groovy.json.JsonSlurper
import org.codehaus.groovy.grails.web.json.JSONObject
import org.codehaus.groovy.grails.web.json.JSONArray
import grails.converters.JSON
import org.codehaus.groovy.grails.commons.ApplicationAttributes 
import org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsDomainBinder
import grails.util.Environment

class BootStrap {
    def grailsApplication
    def JsonvalidatorService
    
    //If only certain packages need to have this domain class validation, include them here
    def allowedPackages = ["com.my.dom"]
                

    def init = { servletContext ->
    	new Person(firstname: "first1").save(failOnError: true)
        
        grailsApplication.domainClasses.each {domain ->

            allowedPackages.each { allowedPackage ->
                def domainName = domain.newInstance().class.name.toString()
                
                if(isAllowed(allowedPackage, domainName)) {
                    //Kill the default constructor so that no one creates an empty object
                    domain.metaClass.constructor = { ->
                        return null
                    }

                    //For a constructor having a single object
                    domain.metaClass.constructor = { JSONObject jsonData -> 
                        def domainList = []
                        domainList << getDomainInstance(domain, jsonData)
                        return domainList
                    }

                    //For a constructor with a list of objects as an array
                    domain.metaClass.constructor = { JSONArray jsonDataArray -> 
                        def domainList = []
                        def instance = domain.newInstance()
                        jsonDataArray.each {
                            domainList << getDomainInstance(domain, it)
                        }
                        return domainList
                    }
                }
            }            
        }        
    }
    def destroy = {
    }
    
    def getDomainInstance(domain, jsonData) {
        
        GrailsDomainBinder domainBinder = new GrailsDomainBinder()

        def instance = domain.newInstance()
        
        def domainName =instance.class.name.toString()
                
        def jsonSlurper = new JsonSlurper()
        
        def jsonObject = jsonSlurper.parseText(jsonData.toString())
                
        def className = domainName.split("\\.")
        //For example, if the domain class name is Entity, then its schema as EntitySchema.json
        def schemaName = className[className.length - 1] + "Schema.json"
        def schemaFile
        if (Environment.current != Environment.DEVELOPMENT) {
            schemaFile = new File( grailsApplication.config.schemas.folder.path + "/" + schemaName )
        } else {
            schemaFile = grailsApplication.mainContext.getResource("/scripts/schemas/" + schemaName).file
        }
                
        def schema = jsonSlurper.parse( schemaFile )
        def validationErrors = JsonvalidatorService.validate(jsonObject, schema)
        if (validationErrors) {
            instance.metaClass.hasSchemaErrors = { -> true }
            instance.metaClass.getSchemaErrors = { -> new groovy.json.JsonBuilder(validationErrors).toString() }
            return instance
        } else {
            instance.metaClass.hasSchemaErrors = { -> false }
            instance.metaClass.getSchemaErrors = { -> "" }
        }
        
        if (domain.metaClass.methods*.name.contains("setCustomMapping")) {
            instance.setCustomMapping(jsonObject)
        }
                
        def idcolumn
        def allowedPack = allowedDLDPackages.find { domainName.startsWith(it) }
        if(allowedPack != null) {
            idcolumn = "id"
        } else {
            idcolumn = "id" + domainBinder.getMapping(domain.clazz).table.getName()
        }
        
        def domainObject
        if(jsonObject.(idcolumn.toString())){  //Update & delete
            if (allowedPack != null) {
                domainObject = instance.class.get((jsonObject.(idcolumn.toString())).toInteger())
            } else {
                domainObject = instance.class.findById((jsonObject.(idcolumn.toString())).toInteger())
            }
            if(domainObject == null) {
                return null
            }
            domainObject.metaClass.hasSchemaErrors = { -> instance.hasSchemaErrors() }
            domainObject.metaClass.getSchemaErrors = { -> instance.getSchemaErrors() }
        } else { // Insert
            domainObject = instance
        }
        
        /* setCustomMapping( ) function is used to modify names and values within the data JSON
         * as required. It takes the data JSON as an input and calls the function and modifies it.
         * However, setCustomDomainMapping( ) is used when a customized mapping between the data
         * JSON and domain class variables is required. For example, when multiple column values
         * of attributes of product/customer has to be combined into one column named "attrib", this
         * function can be used.
         * */
        
        if (domain.metaClass.methods*.name.contains("setCustomDomainMapping")) {
            instance.setCustomDomainMapping(jsonObject, domainObject)
        }
        
        if(utilityService.getActionName() != 'delete') {
            domain.properties.each{ p ->
                if (jsonObject.containsKey("$p.name")) {
                    if(p.type == Date){
                        domainObject."$p.name" = getDate(jsonObject."$p.name")
                    } else {
                        domainObject."$p.name" = jsonObject."$p.name"
                    }
                }
            }
        } else {
            if(allowedPack != null) {
                
            } else {
                domainObject.lastupdatedat = getDate(jsonObject.lastupdatedat)
            }
        }
        
        domainObject.discard()
        return domainObject
    }
    
    /*Dates usually come in the form of string and hence there is a need to validate if it agrees to a set of
    standard formats. Date inputs need special handling */
    def getDate(jsonObjectName) {
        def formatedDate
        if(jsonObjectName){
            def dateFormats = ['yyyy-MM-dd', 'yyyy-MM-dd HH:mm:ss', "yyyy-MM-dd HH:mm:ss SSS"]
            dateFormats.each { dateFormat ->
                try {
                    formatedDate= new Date().parse(dateFormat, jsonObjectName)
                    return false
                } catch(java.text.ParseException e) {
                }
            }
        }
        return formatedDate ? formatedDate : new Date()
    }
    
    /* A logic here can decide which packages and domain need this validation and which ones are to be
    ignored */
    def isAllowed(allowedPackage, domainName){
        return true
    }    
}
