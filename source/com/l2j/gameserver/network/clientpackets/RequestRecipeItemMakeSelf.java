/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package com.l2j.gameserver.network.clientpackets;

import com.l2j.gameserver.controllers.RecipeController;
import com.l2j.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author L2JLastWarrior
 */
public final class RequestRecipeItemMakeSelf extends L2GameClientPacket
{
	private int _id;

	@Override
	protected void readImpl()
	{
		_id = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		
		if (!getClient().getFloodProtectors().getManufacture().tryPerformAction("RecipeMakeSelf"))
    		return;

		
		if(activeChar.getPrivateStoreType() != 0)
		{
			activeChar.sendMessage("Cannot make items while trading");
			return;
		}

		if(activeChar.isInCraftMode())
		{
			activeChar.sendMessage("Currently in Craft Mode");
			return;
		}

		RecipeController.getInstance().requestMakeItem(activeChar, _id);
	}

	@Override
	public String getType()
	{
		return "[C] AF RequestRecipeItemMakeSelf";
	}

}
