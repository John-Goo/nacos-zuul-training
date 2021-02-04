package org.crazyit.cloud;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
	
	@Autowired
	private MemberClient memberClient;
	
	@RequestMapping(value = "/food-sale/{memberId}", method = RequestMethod.GET)
	public String foodSale(@PathVariable Integer memberId) {
		Member m = memberClient.getMember(memberId);
		System.out.println(m.getId() + "---" + m.getName());
		return "success";
	}

}
