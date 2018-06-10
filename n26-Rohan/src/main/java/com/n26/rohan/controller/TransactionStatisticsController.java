package com.n26.rohan.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import com.n26.rohan.model.StatisticsData;
import com.n26.rohan.model.Transaction;
import com.n26.rohan.service.TransactionStatisticsService;

@RestController
public class TransactionStatisticsController implements HandlerExceptionResolver{

	@Autowired
	private TransactionStatisticsService service;
	
	@RequestMapping(method=RequestMethod.POST,path="/transactions")
	@ResponseStatus(HttpStatus.CREATED)
    void addTransaction(@RequestBody Transaction trans) {
		service.addTransaction(trans);
    }
	
	@RequestMapping(method=RequestMethod.GET,path="/statistics")
	StatisticsData getStatistics(){
		return service.getOverallStats();
	}

	@Override
	public ModelAndView resolveException(HttpServletRequest req, HttpServletResponse resp, Object handler,
			Exception ex) {
		 	resp.reset();
	        resp.setCharacterEncoding("UTF-8");
	        resp.setContentType("text/json");
	        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	        ModelAndView model = new ModelAndView(new MappingJackson2JsonView());
            model.addObject("code", HttpStatus.INTERNAL_SERVER_ERROR.value());
            model.addObject("message", ex.getMessage());
            return model;
	}
}
