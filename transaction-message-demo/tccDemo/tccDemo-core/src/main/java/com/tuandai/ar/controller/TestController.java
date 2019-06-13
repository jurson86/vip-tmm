package com.tuandai.ar.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
	@RequestMapping({ "/test" })
	@ResponseBody
	public User test(Integer t) {
		User user = new User();
		if (t > 0) {
			user.setUserName("testUser");
			user.setPassword("00000000");
		} else {
			throw new RuntimeException("运行时出现异常");
		}
		return user;
	}

	public class User {
		private String userName;
		private String password;

		public String getUserName() {
			return this.userName;
		}

		public void setUserName(String userName) {
			this.userName = userName;
		}

		public String getPassword() {
			return this.password;
		}

		public void setPassword(String password) {
			this.password = password;
		}

		@Override
		public String toString() {
			return "User [userName=" + this.userName + ", password=" + this.password + "]";
		}
	}
}