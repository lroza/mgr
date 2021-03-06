package edu.agh.lroza.synchronize;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import edu.agh.lroza.common.Id;
import edu.agh.lroza.javacommon.Notice;
import edu.agh.lroza.javacommon.NoticeBoardServerJava;
import edu.agh.lroza.javacommon.ProblemException;
import edu.agh.lroza.javacommon.TitleId;

import com.google.common.collect.ImmutableSet;

public class SynchronizedServerJava implements NoticeBoardServerJava {
    private Set<UUID> loggedUsers = Collections.synchronizedSet(new HashSet<UUID>());
    private Map<Id, Notice> notices = Collections.synchronizedMap(new HashMap<Id, Notice>());

    @Override
    public UUID login(String username, String password) throws ProblemException {
        if (username.equals(password)) {
            UUID token = UUID.randomUUID();
            loggedUsers.add(token);
            return token;
        } else {
            throw new ProblemException("Wrong password");
        }
    }

    @Override
    public void logout(UUID token) throws ProblemException {
        if (!loggedUsers.remove(token)) {
            throw new ProblemException("Invalid token");
        }
    }

    private void validateToken(UUID token) throws ProblemException {
        if (!loggedUsers.contains(token)) {
            throw new ProblemException("Invalid token");
        }
    }

    @Override
    public java.util.Set<Id> listNoticesIds(UUID token) throws ProblemException {
        validateToken(token);
        return ImmutableSet.copyOf(notices.keySet());
    }

    @Override
    public Id addNotice(UUID token, String title, String message) throws ProblemException {
        validateToken(token);
        Id id = new TitleId(title);
        synchronized (notices) {
            if (notices.get(id) == null) {
                notices.put(id, new Notice(title, message));
                return id;
            } else {
                throw new ProblemException("Topic with title '" + title + "' already exists");
            }
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
        synchronized (notices) {
            Notice notice = notices.get(id);
            if (notice == null) {
                throw new ProblemException("There is no such notice '" + id + "'");
            } else {
                Id newId = new TitleId(title);
                if (!newId.equals(id) && notices.containsKey(newId)) {
                    throw new ProblemException("Topic with title '" + title + "' already exists");
                } else {
                    if (!newId.equals(id)) {
                        notices.remove(id);
                    }
                    notices.put(newId, new Notice(title, message));
                    return newId;
                }
            }
        }
    }

    @Override
    public void deleteNotice(UUID token, Id id) throws ProblemException {
        validateToken(token);
        if (notices.remove(id) == null) {
            throw new ProblemException("There is no such notice '" + id + "'");
        }
    }
}
