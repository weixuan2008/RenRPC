package com.hy.tech.points001.proxy01.staticproxy;

public class UserManagerImplProxy implements UserManager {
	// target object
	private UserManager userManager;

	// pass target object by construct
	public UserManagerImplProxy(UserManager userManager) {
		this.userManager = userManager;
	}

	@Override
	public void addUser(String userId, String userName) {
		try {
			// add logging function
			System.out.println("start-->addUser()");
			
			// begin to add user
			userManager.addUser(userId, userName);
			
			// successed to add user and logging again.
			System.out.println("success-->addUser()");
		} catch (Exception e) {
			// failed to add user
			System.out.println("error-->addUser()");
		}
	}

	@Override
	public void delUser(String userId) {
		userManager.delUser(userId);
	}

	@Override
	public String findUser(String userId) {
		userManager.findUser(userId);
		return "zhang san";
	}

	@Override
	public void modifyUser(String userId, String userName) {
		userManager.modifyUser(userId, userName);
	}

}
