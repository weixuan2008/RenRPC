package com.hy.ren.rpc.model;

import java.io.Serializable;

import com.hy.ren.rpc.common.PacketType;

public abstract class Packet implements Serializable{
	private static final long serialVersionUID = -8611591537441242999L;
	private String id;
	private PacketType packetType;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public PacketType getPacketType() {
		return packetType;
	}

	public void setPacketType(PacketType packetType) {
		this.packetType = packetType;
	}
}
