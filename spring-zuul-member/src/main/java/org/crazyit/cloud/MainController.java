package org.crazyit.cloud;

import com.alibaba.fastjson.JSON;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MainController {

	@RequestMapping(value = "/member/{id}", method = RequestMethod.GET, 
			produces = MediaType.APPLICATION_JSON_VALUE)
	public Member getMember(@PathVariable Integer id) {
		Member p = new Member();
		p.setId(id);
		p.setName("angus-"+id);
		System.out.println("会员信息===>"+ JSON.toJSON(p));
		return p;
	}
}
