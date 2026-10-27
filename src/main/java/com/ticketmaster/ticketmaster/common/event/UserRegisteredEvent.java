package com.ticketmaster.ticketmaster.common.event;

import com.ticketmaster.ticketmaster.user.entity.User;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class UserRegisteredEvent extends ApplicationEvent {

    private final User user;

    public UserRegisteredEvent(User user) {
        super(user);
        this.user = user;
    }
}