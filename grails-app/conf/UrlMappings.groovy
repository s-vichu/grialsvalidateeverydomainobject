class UrlMappings {

	static mappings = {

        "/person/list/$id?(.$format)?"(controller:"person", action:"list")
        "/person/create/$id?(.$format)?"(controller:"person", action:"create")

        "/$controller/$action?/$id?(.$format)?"{
            constraints {
                // apply constraints here
            }
        }

        "/"(view:"/index")
        "500"(view:'/error')
	}
}
