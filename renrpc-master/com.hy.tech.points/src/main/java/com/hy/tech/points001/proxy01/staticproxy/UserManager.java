package com.hy.tech.points001.proxy01.staticproxy;

public interface UserManager {
	void addUser(String userId, String userName);

	void delUser(String userId);

	String findUser(String userId);

	void modifyUser(String userId, String userName);
}