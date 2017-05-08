package com.my

import grails.converters.JSON
import org.springframework.web.servlet.support.RequestContextUtils
import groovy.json.JsonSlurper


class SiteFilters {
    
    def grailsApplication
    def JsonvalidatorService
    
    def filters = {
        
        /*SchemaValidationFilters(controller: '*', action: '*') {
            before = {
                if(request.method == 'POST') {
                    def jsonSlurper = new JsonSlurper()
                    def schemaName = controllerName+'Schema.json'
                    def schemaFile = grailsApplication.mainContext.getResource("/scripts/schemas/" + schemaName).file
                    def schema = jsonSlurper.parse( schemaFile )
                    def validationErrors = JsonvalidatorService.validate(request.JSON, schema)
                    if (validationErrors.size() != 0) {
                        def result = [:]
                        result.status = false

                        result.message = makeErrMessage(validationErrors, request)
                        response.status = 400
                        result.data = request.JSON
                        render result as JSON
                        return false
                    }
                    return true
                }
            }
      
        } */
    }

    def makeErrMessage(errors, request) {

        def message = ""

        errors.each{
            if (message != "") message += ";" 
            message = message + it.instance + " " + grailsApplication.mainContext.getMessage(it.message,null,
                                            "Input data does not conform to its schema",
                                            RequestContextUtils.getLocale(request))
        }
        message
    }    
}
