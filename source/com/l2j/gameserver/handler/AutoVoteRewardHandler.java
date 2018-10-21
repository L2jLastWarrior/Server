package com.l2j.gameserver.handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import com.l2j.Config;
import com.l2j.gameserver.model.L2World;
import com.l2j.gameserver.model.actor.instance.L2PcInstance;
import com.l2j.gameserver.model.entity.Announcements;
import com.l2j.gameserver.thread.ThreadPoolManager;

public class AutoVoteRewardHandler
{
	protected static final Logger _log = Logger.getLogger(AutoVoteRewardHandler.class.getName());
	
	//private String GameServers200_ServerName = Config.GAMESERVERS200_SERVER_NAME;
	
	private int hopzoneVotesCount = 0;
	private int topzoneVotesCount = 0;
	//private int gameservers200VotesCount = 0;
	
	protected List<String> already_rewarded;
	
	protected static boolean topzone = false;
	protected static boolean hopzone = false;
	//protected static boolean gameservers200 = false;
	
	private AutoVoteRewardHandler()
	{
		_log.info("Vote Reward System Initiated.");
		
		if(hopzone){
			int hopzone_votes = getHopZoneVotes();
			
			if(hopzone_votes == -1){
				hopzone_votes = 0;
			}
			
			setHopZoneVoteCount(hopzone_votes);
		}
		
		if(topzone){
			int topzone_votes = getTopZoneVotes();
			
			if(topzone_votes == -1){
				topzone_votes = 0;
			}
			
			setTopZoneVoteCount(topzone_votes);
		}
		/*
		if(gameservers200){
			int gameservers200_votes = getGameServers200Votes();
			
			if(gameservers200_votes == -1){
				gameservers200_votes = 0;
			}
			
			setGameServers200VoteCount(gameservers200_votes);
		}
		*/
		
		ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new AutoReward(), Config.VOTES_SYSYEM_INITIAL_DELAY, Config.VOTES_SYSYEM_STEP_DELAY);
	}

	protected class AutoReward implements Runnable
	{
		@Override
		public void run()
		{
			int minutes = (Config.VOTES_SYSYEM_STEP_DELAY/1000)/60;
			
			if(hopzone){
				int hopzone_votes = getHopZoneVotes();
				
				if(hopzone_votes != -1){
					_log.info("AutoRewardSystem: Server HOPZONE Votes: " + hopzone_votes);
					Announcements.getInstance().gameAnnounceToAll("AutoRewardSystem: Actual HOPZONE Votes are " + hopzone_votes + "...");
					
					if (hopzone_votes != 0 && hopzone_votes >= getHopZoneVoteCount() + Config.VOTES_FOR_REWARD)
					{
						already_rewarded = new ArrayList<String>();
						
						Collection<L2PcInstance> pls = L2World.getInstance().getAllPlayers();

						Announcements.getInstance().gameAnnounceToAll("AutoRewardSystem: Great Work! Check your inventory for Reward!!");
						
						//L2ItemInstance item;
						for (L2PcInstance player : pls)
						{
							if (player != null && !player.isOffline() && player.isOnline()==1)
							{
								if(player._active_boxes<=1 || (player._active_boxes>1 && checkSingleBox(player))){
									
									Set<Integer> items = Config.VOTES_REWARDS_LIST.keySet();
									for (Integer i : items)
									{
										//item = player.getInventory().getItemByItemId(i);

										//TODO: check on maxstack for item
										player.addItem("reward", i, Config.VOTES_REWARDS_LIST.get(i), player, true);

									}
									
								}
							}
						}
						setHopZoneVoteCount(hopzone_votes);
					}
					Announcements.getInstance().gameAnnounceToAll("AutoRewardSystem: Next HOPZONE Reward in "+minutes+" minutes at " + (getHopZoneVoteCount() + Config.VOTES_FOR_REWARD) + " Votes!!");
					//site web
					Announcements.getInstance().gameAnnounceToAll("OurSiteLink: "+Config.SERVER_WEB_SITE);
					
				}
				
			}
			/*
			if(gameservers200){
				int gameservers200_votes = getGameServers200Votes();
				
				if(gameservers200_votes != -1){
					_log.info("AutoRewardSystem: Server HOPZONE Votes: " + gameservers200_votes);
					Announcements.getInstance().gameAnnounceToAll("AutoRewardSystem: Actual HOPZONE Votes are " + gameservers200_votes + "...");
					
					if (gameservers200_votes != 0 && gameservers200_votes >= getGameServers200VoteCount() + Config.VOTES_FOR_REWARD)
					{
						already_rewarded = new ArrayList<String>();
						
						Collection<L2PcInstance> pls = L2World.getInstance().getAllPlayers();

						Announcements.getInstance().gameAnnounceToAll("AutoRewardSystem: Great Work! Check your inventory for Reward!!");
						
						//L2ItemInstance item;
						for (L2PcInstance player : pls)
						{
							if (player != null && !player.isOffline() && player.isOnline()==1)
							{
								if(player._active_boxes<=1 || (player._active_boxes>1 && checkSingleBox(player))){
									
									Set<Integer> items = Config.VOTES_REWARDS_LIST.keySet();
									for (Integer i : items)
									{
										//item = player.getInventory().getItemByItemId(i);

										//TODO: check on maxstack for item
										player.addItem("reward", i, Config.VOTES_REWARDS_LIST.get(i), player, true);

									}
									
								}
							}
						}
						setHopZoneVoteCount(gameservers200_votes);
					}
					Announcements.getInstance().gameAnnounceToAll("AutoRewardSystem: Next GameServers200 Reward in "+minutes+" minutes at " + (getGameServers200VoteCount() + Config.VOTES_FOR_REWARD) + " Votes!!");
					//site web
					Announcements.getInstance().gameAnnounceToAll("OurSiteLink: "+Config.SERVER_WEB_SITE);
					
				}
				
			}
			*/
				
			if(topzone && hopzone /* && gameservers200 */ && Config.VOTES_SYSYEM_STEP_DELAY>0)
				try
				{
					Thread.sleep(Config.VOTES_SYSYEM_STEP_DELAY/2);
				}
				catch(InterruptedException e)
				{
					if(Config.ENABLE_ALL_EXCEPTIONS)
						e.printStackTrace();
				}
				
			if(topzone){
				int topzone_votes = getTopZoneVotes();
				
				if(topzone_votes != -1){
					
					_log.info("AutoRewardSystem: Server TOPZONE Votes: " + topzone_votes);
					Announcements.getInstance().gameAnnounceToAll("AutoRewardSystem: Actual TOPZONE Votes are " + topzone_votes + "...");
					
					if (topzone_votes != 0 && topzone_votes >= getTopZoneVoteCount() + Config.VOTES_FOR_REWARD)
					{
						already_rewarded = new ArrayList<String>();
						
						Collection<L2PcInstance> pls = L2World.getInstance().getAllPlayers();

						Announcements.getInstance().gameAnnounceToAll("AutoRewardSystem: Great Work! Check your inventory for Reward!!");
						
						//L2ItemInstance item;
						for (L2PcInstance player : pls)
						{
							if (player != null && !player.isOffline() && player.isOnline()==1)
							{
								if(player._active_boxes<=1 || (player._active_boxes>1 && checkSingleBox(player))){
									
									Set<Integer> items = Config.VOTES_REWARDS_LIST.keySet();
									for (Integer i : items)
									{
										//item = player.getInventory().getItemByItemId(i);

										//TODO: check on maxstack for item
										player.addItem("reward", i, Config.VOTES_REWARDS_LIST.get(i), player, true);

									}
									
								}
							}
						}
						setTopZoneVoteCount(topzone_votes);
					}
					
					Announcements.getInstance().gameAnnounceToAll("AutoRewardSystem: Next TOPZONE Reward in "+minutes+" minutes at " + (getTopZoneVoteCount() + Config.VOTES_FOR_REWARD) + " Votes!!");
					//site web
					Announcements.getInstance().gameAnnounceToAll("OurSiteLink: "+Config.SERVER_WEB_SITE);
					
				}
					
				
			}
			
		}
	}

	protected boolean checkSingleBox(L2PcInstance player){
		
		if(player.getClient()!=null && player.getClient().getConnection()!=null && !player.getClient().getConnection().isClosed() && !player.isOffline()){
			
			String playerip = player.getClient().getConnection().getInetAddress().getHostAddress();
			
			if(already_rewarded.contains(playerip))
				return false;
			already_rewarded.add(playerip);
			return true;
		}
		
		//if no connection (maybe offline shop) don't reward
		return false;
	}
	//HopZone Access Site Settings
	protected int getHopZoneVotes()
	{
		int votes = -1;
		URL url = null;
		URLConnection con = null;
		InputStream is = null;
		InputStreamReader isr = null;
		BufferedReader in = null;
		try
		{
			url = new URL(Config.VOTES_SITE_HOPZONE_URL);
			con = url.openConnection();    
			con.addRequestProperty("User-Agent", "Mozilla/4.76");
			is = con.getInputStream();
			isr = new InputStreamReader(is);		    
			in = new BufferedReader(isr);
			String inputLine;
			while ((inputLine = in.readLine()) != null)
			{
				if (inputLine.contains("no steal make love")||inputLine.contains("no votes here")||inputLine.contains("bang, you don't have votes")||inputLine.contains("la vita e bella"))
				{
					votes = Integer.valueOf(inputLine.split(">")[2].replace("</span", ""));
					break;
				}
			}
		}
		catch (Exception e)
		{
			_log.info("AutoRewardSystem: Server HOPZONE is offline or something is wrong in link");
			Announcements.getInstance().gameAnnounceToAll("AutoRewardSystem: HOPZONE is offline. We will check reward as it will be online again");
			//e.printStackTrace();
		}
		finally
		{
			if(in!=null)
				try
				{
					in.close();
				}
				catch(IOException e1)
				{
					e1.printStackTrace();
				}
			if(isr!=null)
				try
				{
					isr.close();
				}
				catch(IOException e1)
				{
					e1.printStackTrace();
				}
			if(is!=null)
				try
				{
					is.close();
				}
				catch(IOException e1)
				{
					e1.printStackTrace();
				}
			
		}
		return votes;
	}
	//TopZone Access Site Settings
	protected int getTopZoneVotes()
	{
		int votes = -1;
		URL url = null;
		URLConnection con = null;
		InputStream is = null;
		InputStreamReader isr = null;
		BufferedReader in = null;
		try
		{
			url = new URL(Config.VOTES_SITE_TOPZONE_URL);
			con = url.openConnection();    
			con.addRequestProperty("User-Agent", "Mozilla/4.76");
			is = con.getInputStream();
			isr = new InputStreamReader(is);		    
			in = new BufferedReader(isr);
			String inputLine;
			while ((inputLine = in.readLine()) != null)
			{
				if (inputLine.contains("Votes:"))
				{
					String votesLine = inputLine;
					
					votes = Integer.valueOf(votesLine.split(">")[3].replace("</div", ""));
					break;
				}
			}
		}
		catch (Exception e)
		{
			_log.info("AutoRewardSystem: Server TOPZONE is offline or something is wrong in link");
			Announcements.getInstance().gameAnnounceToAll("AutoRewardSystem: TOPZONE is offline. We will check reward as it will be online again");
			//e.printStackTrace();
		}
		finally
		{
			if(in!=null)
				try
				{
					in.close();
				}
				catch(IOException e1)
				{
					e1.printStackTrace();
				}
			if(isr!=null)
				try
				{
					isr.close();
				}
				catch(IOException e1)
				{
					e1.printStackTrace();
				}
			if(is!=null)
				try
				{
					is.close();
				}
				catch(IOException e1)
				{
					e1.printStackTrace();
				}
		}
		return votes;
	}
	
	//GameServers200 Access Site Settings
	/*
	protected int getGameServers200Votes()
	{
		int votes = -1;
		URL url = null;
		URLConnection con = null;
		InputStream is = null;
		InputStreamReader isr = null;
		BufferedReader in = null;
		try
		{
			url = new URL(Config.VOTES_SITE_GAMESERVERs200_URL);
			con = url.openConnection();    
			con.addRequestProperty("User-Agent", "Mozilla/4.76");
			is = con.getInputStream();
			isr = new InputStreamReader(is);		    
			in = new BufferedReader(isr);
			String inputLine;
			while ((inputLine = in.readLine()) != null)
			{
				if (inputLine.contains(GameServers200_ServerName)||inputLine.contains("IN"))
				{
					String votesLine = inputLine;
					
					votes = Integer.valueOf(votesLine.split("</span></b><br /> <span>")[3].replace("</span></td>", ""));
					break;
				}
			}
		}
		catch (Exception e)
		{
			_log.info("AutoRewardSystem: Server GameServers200 is offline or something is wrong in link");
			Announcements.getInstance().gameAnnounceToAll("AutoRewardSystem: GameServers200 is offline. We will check reward as it will be online again");
			//e.printStackTrace();
		}
		finally
		{
			if(in!=null)
				try
				{
					in.close();
				}
				catch(IOException e1)
				{
					e1.printStackTrace();
				}
			if(isr!=null)
				try
				{
					isr.close();
				}
				catch(IOException e1)
				{
					e1.printStackTrace();
				}
			if(is!=null)
				try
				{
					is.close();
				}
				catch(IOException e1)
				{
					e1.printStackTrace();
				}
			
		}
		return votes;
	}
	*/
	
	protected void setHopZoneVoteCount(int voteCount)
	{
		hopzoneVotesCount = voteCount;
	}

	protected int getHopZoneVoteCount()
	{
		return hopzoneVotesCount;
	}

	protected void setTopZoneVoteCount(int voteCount)
	{
		topzoneVotesCount = voteCount;
	}

	protected int getTopZoneVoteCount()
	{
		return topzoneVotesCount;
	}
	/*
	protected void setGameServers200VoteCount(int voteCount)
	{
		gameservers200VotesCount = voteCount;
	}

	protected int getGameServers200VoteCount()
	{
		return gameservers200VotesCount;
	}
	*/
	
	public static AutoVoteRewardHandler getInstance()
	{
		if(Config.VOTES_SITE_HOPZONE_URL != null && !Config.VOTES_SITE_HOPZONE_URL.equals("")){
			hopzone = true;
		}
		
		if(Config.VOTES_SITE_TOPZONE_URL != null && !Config.VOTES_SITE_TOPZONE_URL.equals("")){
			topzone = true;
		}
		/*
		if(Config.VOTES_SITE_GAMESERVERs200_URL != null && !Config.VOTES_SITE_GAMESERVERs200_URL.equals("")){
			gameservers200 = true;
		}
		*/
		
		if(topzone || hopzone /*|| gameservers200*/)
			return SingletonHolder._instance;
		return null;
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final AutoVoteRewardHandler    _instance       = new AutoVoteRewardHandler();
	}
}