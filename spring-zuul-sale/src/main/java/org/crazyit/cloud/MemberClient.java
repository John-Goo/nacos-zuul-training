package org.crazyit.cloud;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient("spring-zuul-member")
public interface MemberClient {

	@RequestMapping(value = "/member/{id}", method = RequestMethod.GET)
	public Member getMember(@PathVariable("id") Integer id);
}
