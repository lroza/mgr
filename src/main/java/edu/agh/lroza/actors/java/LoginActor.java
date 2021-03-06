package edu.agh.lroza.actors.java;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import akka.actor.ActorRef;
import akka.actor.Channel;
import akka.actor.UntypedActor;
import edu.agh.lroza.javacommon.ProblemException;

@SuppressWarnings({"unchecked", "rawtypes"})
public class LoginActor extends UntypedActor {
    private Set<UUID> loggedUsers = new HashSet<>();
    private Channel noticesActor;

    public LoginActor(ActorRef noticesActor) {
        this.noticesActor = noticesActor;
    }

    public void onReceive(Object message) {
        if (message instanceof NoticeActorMessage) {
            NoticeActorMessage noticeMessage = (NoticeActorMessage) message;
            if (loggedUsers.contains(noticeMessage.getToken())) {
                Channel noticeActor = noticeMessage.getActor();
                if (noticeActor == null || !noticeActor.tryTell(noticeMessage, getContext().getChannel())) {
                    getContext().reply(new ProblemException("There is no such notice"));
                }
            } else {
                getContext().reply(new ProblemException("Please log in"));
            }
        } else if (message instanceof NoticesActorMessage) {
            NoticesActorMessage noticeMessage = (NoticesActorMessage) message;
            if (loggedUsers.contains(noticeMessage.getToken())) {
                noticesActor.tell(noticeMessage, getContext().getChannel());
            } else {
                getContext().reply(new ProblemException("Please log in"));
            }
        } else if (message instanceof Login) {
            Login login = (Login) message;
            if (login.getUsername().equals(login.getPassword())) {
                UUID token = UUID.randomUUID();
                loggedUsers.add(token);
                getContext().reply(token);
            } else {
                getContext().reply(new ProblemException("Wrong password"));
            }
        } else if (message instanceof Logout) {
            Logout logout = (Logout) message;
            if (loggedUsers.contains(logout.getToken())) {
                loggedUsers.remove(logout.getToken());
                getContext().reply(null);
            } else {
                getContext().reply(new ProblemException("Invalid token"));
            }
        } else {
            throw new IllegalArgumentException("Unknown message: " + message);
        }
    }
}