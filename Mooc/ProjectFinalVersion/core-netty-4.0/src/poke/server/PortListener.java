package poke.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

public class PortListener implements ChannelFutureListener
{

	protected static Logger logger = LoggerFactory.getLogger("server");
	private String id;
	
	public PortListener(String id)
	{
		this.id=id;
	}
	
	public String getListenerID()
	{
		return this.id;
	}
	
	@Override
	public void operationComplete(ChannelFuture arg0) throws Exception {
		// TODO Auto-generated method stub
		logger.info("Inside Port Listener");
		
	}
	
}