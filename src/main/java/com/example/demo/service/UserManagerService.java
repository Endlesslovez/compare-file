package com.example.demo.service;

import com.example.demo.entity.UserMng;

public interface UserManagerService {
    /**
     * Creates a new user with the given username and password.
     *
     * @param username the username of the new user
     * @param password the password of the new user
     * @return the created UserMng object
     */
    UserMng createUser(String username, String password);

    UserMng findByUsernameAndPassword(String username, String password);

}
