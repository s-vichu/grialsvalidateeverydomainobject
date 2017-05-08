package com.my

import grails.converters.JSON

class Person {
    
    
    Long id
    String firstname
    String lastname
    Long age
    Date dob
    String department

    String schema
    
    
    static transients = ['schema']
    

    static mapping = {
        datasource 'ALL'
        table "person"
        id name: "id", column: "id", generator:'identity'
        version false
    }    


    static constraints = {
        firstname nullable: true
        lastname nullable: true
        age nullable: true
        dob nullable: true
        department nullable: true
    }
    
    /* Use grails domain validation feature to set which of the domain field is in error.
     * Other additional validations can be added here too */
    def myValidate() {
        if(this.hasSchemaErrors()){
            getErrors().rejectValue("schema", this.getSchemaErrors())
            return false
        }
        
        return true
    }    

}
