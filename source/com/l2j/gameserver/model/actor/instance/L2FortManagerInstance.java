/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2j.gameserver.model.actor.instance;

import java.util.StringTokenizer;

import com.l2j.gameserver.ai.CtrlIntention;
import com.l2j.gameserver.model.L2Clan;
import com.l2j.gameserver.network.serverpackets.ActionFailed;
import com.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2j.gameserver.network.serverpackets.WareHouseDepositList;
import com.l2j.gameserver.network.serverpackets.WareHouseWithdrawalList;
import com.l2j.gameserver.templates.L2NpcTemplate;

/**
 * Fortress Foreman implementation used for: Area Teleports, Support Magic, Clan Warehouse, Exp Loss Reduction
 */
public class L2FortManagerInstance extends L2MerchantInstance
{
	protected static final int COND_ALL_FALSE = 0;
	protected static final int COND_BUSY_BECAUSE_OF_SIEGE = 1;
	protected static final int COND_OWNER = 2;

	public L2FortManagerInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	public boolean isWarehouse()
	{
		return true;
	}

	private void sendHtmlMessage(L2PcInstance player, NpcHtmlMessage html)
	{
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%npcId%", String.valueOf(getNpcId()));
		player.sendPacket(html);
		html = null;
	}

	@Override
	public void onAction(L2PcInstance player)
	{
		if(!canTarget(player))
			return;

		player.setLastFolkNPC(this);

		// Check if the L2PcInstance already target the L2NpcInstance
		if(this != player.getTarget())
		{
			// Set the target of the L2PcInstance player
			player.setTarget(this);
		}
		else
		{
			// Calculate the distance between the L2PcInstance and the L2NpcInstance
			if(!canInteract(player))
			{
				// Notify the L2PcInstance AI with AI_INTENTION_INTERACT
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
			}
			else
			{
				showMessageWindow(player);
			}
		}
		// Send a Server->Client ActionFailed to the L2PcInstance in order to
		// avoid that the client wait another packet
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		//SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");  //not used???

		int condition = validateCondition(player);

		// BypassValidation Exploit plug.
		if(player.getLastFolkNPC().getObjectId() != getObjectId())
			return;

		if(condition <= COND_ALL_FALSE || condition == COND_BUSY_BECAUSE_OF_SIEGE)
			return;

		else if(condition == COND_OWNER)
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			String actualCommand = st.nextToken(); // Get actual command

			String val = "";
			if(st.countTokens() >= 1)
			{
				val = st.nextToken();
			}

			if(actualCommand.equalsIgnoreCase("banish_foreigner"))
			{
				NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());

				if((player.getClanPrivileges() & L2Clan.CP_CS_DISMISS) == L2Clan.CP_CS_DISMISS)
				{
					if(val.isEmpty())
					{
						html.setFile("data/html/fortress/foreman-expel.htm");
					}
					else
					{
						getFort().banishForeigners(); // Move non-clan members off fortress area
						html.setFile("data/html/fortress/foreman-expeled.htm");
					}
				}
				else
				{
					html.setFile("data/html/fortress/foreman-noprivs.htm");
				}

				html.replace("%objectId%", String.valueOf(getObjectId()));
				player.sendPacket(html);

				html = null;
				return;
			}
			else if(actualCommand.equalsIgnoreCase("manage_vault"))
			{
				if((player.getClanPrivileges() & L2Clan.CP_CL_VIEW_WAREHOUSE) == L2Clan.CP_CL_VIEW_WAREHOUSE)
				{
					if(val.equalsIgnoreCase("deposit"))
					{
						showVaultWindowDeposit(player);
					}
					else if(val.equalsIgnoreCase("withdraw"))
					{
						showVaultWindowWithdraw(player);
					}
					else
					{
						NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
						html.setFile("data/html/fortress/foreman-vault.htm");
						sendHtmlMessage(player, html);

						html = null;
					}
				}
				else
				{
					NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
					html.setFile("data/html/fortress/foreman-noprivs.htm");
					html.replace("%objectId%", String.valueOf(getObjectId()));
					player.sendPacket(html);

					html = null;
					return;
				}
				return;
			}
			else if(actualCommand.equalsIgnoreCase("operate_door")) // Door
			// Control
			{
				if((player.getClanPrivileges() & L2Clan.CP_CS_OPEN_DOOR) == L2Clan.CP_CS_OPEN_DOOR)
				{
					if(!val.isEmpty())
					{
						boolean open = Integer.parseInt(val) == 1;
						while(st.hasMoreTokens())
						{
							getFort().openCloseDoor(player, Integer.parseInt(st.nextToken()), open);
						}
					}

					NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
					html.setFile("data/html/fortress/" + getTemplate().getNpcId() + "-d.htm");
					html.replace("%objectId%", String.valueOf(getObjectId()));
					html.replace("%npcname%", getName());
					player.sendPacket(html);
				}
				else
				{
					NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
					html.setFile("data/html/fortress/foreman-noprivs.htm");
					html.replace("%objectId%", String.valueOf(getObjectId()));
					player.sendPacket(html);
				}
				return;
			}
			else
			{
				super.onBypassFeedback(player, command);
			}

			st = null;
			actualCommand = null;
			val = null;
		}
	}

	private void showMessageWindow(L2PcInstance player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		String filename = "data/html/fortress/foreman-no.htm";

		int condition = validateCondition(player);
		if(condition > COND_ALL_FALSE)
		{
			if(condition == COND_BUSY_BECAUSE_OF_SIEGE)
			{
				filename = "data/html/fortress/foreman-busy.htm"; // Busy because of siege
			}
			else if(condition == COND_OWNER)
			{
				filename = "data/html/fortress/foreman.htm"; // Owner message window
			}
		}

		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%npcname%", getName());
		player.sendPacket(html);

		filename = null;
		html = null;
	}

	private void showVaultWindowDeposit(L2PcInstance player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		player.setActiveWarehouse(player.getClan().getWarehouse());
		player.sendPacket(new WareHouseDepositList(player, WareHouseDepositList.CLAN));
	}

	private void showVaultWindowWithdraw(L2PcInstance player)
	{
		if(player.isClanLeader() || (player.getClanPrivileges() & L2Clan.CP_CL_VIEW_WAREHOUSE) == L2Clan.CP_CL_VIEW_WAREHOUSE)
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			player.setActiveWarehouse(player.getClan().getWarehouse());
			player.sendPacket(new WareHouseWithdrawalList(player, WareHouseWithdrawalList.CLAN));
		}
		else
		{
			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			html.setFile("data/html/fortress/foreman-noprivs.htm");
			html.replace("%objectId%", String.valueOf(getObjectId()));
			player.sendPacket(html);

			html = null;
			return;
		}
	}

	protected int validateCondition(L2PcInstance player)
	{
		if(getFort() != null && getFort().getFortId() > 0)
		{
			if(player.getClan() != null)
			{
				if(getFort().getSiege().getIsInProgress())
					return COND_BUSY_BECAUSE_OF_SIEGE; // Busy because of siege
				else if(getFort().getOwnerId() == player.getClanId()) // Clan owns fortress
					return COND_OWNER; // Owner
			}
		}
		return COND_ALL_FALSE;
	}
}
