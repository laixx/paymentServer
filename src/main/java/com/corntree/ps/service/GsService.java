package com.corntree.ps.service;

import java.util.Map;
import java.util.TreeMap;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.corntree.ps.domain.Session;


@Service
public class GsService {
	public static final Logger logger = LoggerFactory.getLogger(GsService.class);
	
	private Map<Integer, Session> mapAliveGsSession;
	
	@PostConstruct
	public void init() {
		mapAliveGsSession = new TreeMap<Integer, Session>();
	}
	
	public Session getSession(int serverId) {
		return mapAliveGsSession.get(serverId);
	}
	
	public boolean addSession(Session s) {
		if (getSession(s.getServerId()) != null) {
			return false;
		}
		mapAliveGsSession.put(s.getServerId(), s);
		return true;
	}
	
	public void removeSession(int serverId) {
		mapAliveGsSession.remove(serverId);
	}
}
