package org.search.query.controller;

import javax.annotation.Resource;

import org.search.query.service.SearchService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

/**
 * Created by susansun on 2/21/16.
 */
@Controller
public class SearchController {
	
	
	@Resource
	private SearchService searchService;

    @RequestMapping(value="/search", method=RequestMethod.POST)
    public @ResponseBody String handleFileUpload( @RequestParam("file") MultipartFile file)
    {
		return null;
    	
    	
    }

}