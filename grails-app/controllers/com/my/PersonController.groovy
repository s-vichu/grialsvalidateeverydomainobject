package com.my

import grails.converters.JSON

class PersonController {

	static allowedMethods = [create:"POST", delete: "POST", list: "GET", update:"POST"]
    static responseFormats = [
        create: ['json'],
        delete: ['json'],
        list: ['json'],
        update:['json']
    ]	

    def index() { }

    def list() {
    	def result = [:]

    	result.data = Person.findAll()

    	response.status = 200

    	result.status = true
    	result.message = ""

    	respond result
    }

    def create() {
        def result = [:]
        result.status = true
        result.message = ""
        
        def person = new Person(request.JSON)
        
        /* Strictly speaking the following logic needs to be in service. For the sake of 
         * simplicity, I have omitted service here */
        
        if (person.first().myValidate() == false) {

            result.status = false
            result.message = getErrorMessage(person.errors[0], person[0])
            
            response.status = 400
            result.data = request.JSON
            respond result
            return false
        }
                
		response.status = 200
		result.data = request.JSON
		respond result
        return true
    }
    
    private def getErrorMessage(errors, domainObject) {
        def errMessage
        if (errors['schema']) {
            def validationErrors = JSON.parse(domainObject.getSchemaErrors())
            errMessage = validationErrors.instance
        }
        return errMessage
    }
}
