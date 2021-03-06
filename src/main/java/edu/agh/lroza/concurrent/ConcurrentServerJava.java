package edu.agh.lroza.concurrent;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import edu.agh.lroza.common.Id;
import edu.agh.lroza.javacommon.Notice;
import edu.agh.lroza.javacommon.NoticeBoardServerJava;
import edu.agh.lroza.javacommon.ProblemException;

import com.google.common.collect.ImmutableSet;

public class ConcurrentServerJava implements NoticeBoardServerJava {
    private final Object o = new Object();
    private ConcurrentMap<UUID, Object> loggedUsers = new ConcurrentHashMap<>();
    private ConcurrentMap<String, Boolean> titleSet = new ConcurrentHashMap<>();
    private ConcurrentMap<Id, Notice> notices = new ConcurrentHashMap<>();


    private void validateToken(UUID token) throws ProblemException {
        if (!loggedUsers.containsKey(token)) {
            throw new ProblemException("Invalid token");
        }
    }

    @Override
    public UUID login(String username, String password) throws ProblemException {
        if (username.equals(password)) {
            UUID token = UUID.randomUUID();
            loggedUsers.put(token, o);
            return token;
        } else {
            throw new ProblemException("Wrong password");
        }
    }

    @Override
    public void logout(UUID token) throws ProblemException {
        if (loggedUsers.remove(token) == null) {
            throw new ProblemException("Invalid token");
        }
    }

    @Override
    public Set<Id> listNoticesIds(UUID token) throws ProblemException {
        validateToken(token);
        return ImmutableSet.copyOf(notices.keySet());
    }

    @Override
    public Id addNotice(UUID token, String title, String message) throws ProblemException {
        validateToken(token);
        if (titleSet.putIfAbsent(title, false) == null) {
            Id id = LongIdJ.get();
            notices.put(id, new Notice(title, message));
            return id;
        } else {
            throw new ProblemException("Topic with title '" + title + "' already exists");
        }
    }

    @Override
    public Notice getNotice(UUID token, Id id) throws ProblemException {
        validateToken(token);
        Notice notice = notices.get(id);
        if (notice == null) {
            throw new ProblemException("There is no such notice '" + id + "'");
        } else {
            return notice;
        }
    }

    @Override
    public Id updateNotice(UUID token, Id id, String title, String message) throws ProblemException {
        validateToken(token);
        Notice oldNotice = notices.get(id);
        if (titleSet.putIfAbsent(title, true) != null &&
                !(oldNotice != null && oldNotice.getTitle().equals(title) && !reserveTitle(title))) {
            throw new ProblemException("There is no such notice '" + id + "'");
        } else {
            Notice previous = notices.replace(id, new Notice(title, message));
            if (previous == null) {
                titleSet.remove(title);
                throw new ProblemException("There is no such notice '" + id + "'");
            } else {
                titleSet.put(title, false);
                if (!previous.getTitle().equals(title)) {
                    titleSet.remove(previous.getTitle());
                }
                return id;
            }
        }
    }

    private Boolean reserveTitle(String title) {
        Boolean put = titleSet.put(title, true);
        if (put == null) {
            return false;
        } else {
            return put;
        }
    }

    @Override
    public void deleteNotice(UUID token, Id id) throws ProblemException {
        validateToken(token);
        Notice previous = notices.remove(id);
        if (previous == null) {
            throw new ProblemException("There is no such notice '" + id + "'");
        } else {
            while (titleSet.containsKey(previous.getTitle()) && !titleSet.remove(previous.getTitle(), false)) {
            }
        }
    }
}