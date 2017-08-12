package com.hy.tech.points003.netty17.heartbeat.client;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.hyperic.sigar.CpuPerc;
import org.hyperic.sigar.Mem;
import org.hyperic.sigar.Sigar;

import com.hy.tech.points003.netty15.heartbeat.common.RequestInfo;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.ScheduledFuture;

public class ClienHeartBeatHandler extends ChannelInboundHandlerAdapter {

	private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	private ScheduledFuture<?> heartBeat; // 定时任务
	// 主动向服务器发送认证信息
	private InetAddress addr;
	private static final String SUCCESS_KEY = "auth_success_key";

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		addr = InetAddress.getLocalHost();
		// String ip = addr.getHostAddress();
		String ip = "127.0.0.1";
		String key = "1234";
		// 证书
		String auth = ip + "," + key;
		// 发送认证
		ctx.writeAndFlush(auth);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		try {
			if (msg instanceof String) {
				String ret = (String) msg;
				if (SUCCESS_KEY.equals(ret)) {
					// 收到认证 确认信息，设置每隔5秒发送心跳消息
					this.heartBeat = (ScheduledFuture<?>) this.scheduler.scheduleWithFixedDelay(new HeartBeatTask(ctx), 0, 5,TimeUnit.SECONDS);
					System.out.println(msg);
				} else {
					// 收到心跳包 确认信息
					System.out.println(msg);
				}
			}
		} finally {
			// 只读, 需要手动释放引用计数
			ReferenceCountUtil.release(msg);
		}
	}

	private class HeartBeatTask implements Runnable {
        private final ChannelHandlerContext ctx;
        public HeartBeatTask(final ChannelHandlerContext ctx) {
            this.ctx = ctx;
        }
        @Override
        public void run() {
            try {
                RequestInfo info = new RequestInfo();
                //ip
                info.setIp(addr.getHostAddress());
                Sigar sigar = new Sigar();
                //cpu prec
                CpuPerc cpuPerc = sigar.getCpuPerc();
                HashMap<String, Object> cpuPercMap = new HashMap<String, Object>();
                cpuPercMap.put("combined", cpuPerc.getCombined());
                cpuPercMap.put("user", cpuPerc.getUser());
                cpuPercMap.put("sys", cpuPerc.getSys());
                cpuPercMap.put("wait", cpuPerc.getWait());
                cpuPercMap.put("idle", cpuPerc.getIdle());
                // memory
                Mem mem = sigar.getMem();
                HashMap<String, Object> memoryMap = new HashMap<String, Object>();
                memoryMap.put("total", mem.getTotal() / 1024L);
                memoryMap.put("used", mem.getUsed() / 1024L);
                memoryMap.put("free", mem.getFree() / 1024L);
                info.setCpuPercMap(cpuPercMap);
                info.setMemoryMap(memoryMap);
                
                ctx.writeAndFlush(info);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
	}
        
	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		ctx.flush();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable t) throws Exception {
		t.printStackTrace();
        // 取消定时发送心跳包的任务
        if (heartBeat != null) {
            heartBeat.cancel(true);
            heartBeat = null;
        }
        ctx.fireExceptionCaught(t);
	}

}
