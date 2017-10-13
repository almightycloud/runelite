/*
 * Copyright (c) 2017, Tyler <http://github.com/tylerthardy>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.runepouch;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.ItemID;
import net.runelite.api.Point;
import net.runelite.api.Varbits;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.RuneLite;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RunepouchOverlay extends Overlay
{
	private static final Logger logger = LoggerFactory.getLogger(RunepouchOverlay.class);

	private static final Varbits[] AMOUNT_VARBITS =
	{
		Varbits.RUNE_POUCH_AMOUNT1, Varbits.RUNE_POUCH_AMOUNT2, Varbits.RUNE_POUCH_AMOUNT3
	};
	private static final Varbits[] RUNE_VARBITS =
	{
		Varbits.RUNE_POUCH_RUNE1, Varbits.RUNE_POUCH_RUNE2, Varbits.RUNE_POUCH_RUNE3
	};

	private final Client client = RuneLite.getClient();
	private final RuneImageCache runeImageCache = new RuneImageCache();
	private final Runepouch plugin;
	private final RunepouchConfig config;

	public RunepouchOverlay(Runepouch plugin)
	{
		super(OverlayPosition.DYNAMIC);
		this.plugin = plugin;
		this.config = plugin.getConfig();
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!config.enabled()
			|| client.getGameState() != GameState.LOGGED_IN
			|| client.getWidget(WidgetInfo.LOGIN_CLICK_TO_PLAY_SCREEN) != null)
		{
			return null;
		}

		Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);

		if (inventoryWidget == null || inventoryWidget.isHidden())
		{
			return null;
		}

		graphics.setFont(FontManager.getRunescapeSmallFont());

		for (WidgetItem item : inventoryWidget.getWidgetItems())
		{
			if (item.getId() != ItemID.RUNE_POUCH)
			{
				continue;
			}

			Point location = item.getCanvasLocation();
			if (location == null)
			{
				continue;
			}

			assert AMOUNT_VARBITS.length == RUNE_VARBITS.length;

			for (int i = 0; i < AMOUNT_VARBITS.length; i++)
			{
				Varbits amountVarbit = AMOUNT_VARBITS[i];
				Varbits runeVarbit = RUNE_VARBITS[i];

				int amount = client.getSetting(amountVarbit);
				if (amount <= 0)
				{
					continue;
				}

				graphics.setColor(Color.black);
				graphics.drawString("" + formatNumber(amount), location.getX() + (config.showIcons() ? 13 : 1),
					location.getY() + 14 + graphics.getFontMetrics().getHeight() * i);

				graphics.setColor(config.fontColor());
				graphics.drawString("" + formatNumber(amount), location.getX() + (config.showIcons() ? 12 : 0),
					location.getY() + 13 + graphics.getFontMetrics().getHeight() * i);

				if (!config.showIcons())
				{
					continue;
				}

				int runeId = client.getSetting(runeVarbit);

				BufferedImage runeImg = runeImageCache.getImage(runeId);
				if (runeImg != null)
				{
					OverlayUtil.renderImageLocation(graphics,
						new Point(location.getX(), location.getY() + 2 + (graphics.getFontMetrics().getHeight()) * i),
						runeImg);
				}
			}
		}
		return null;
	}

	private static String formatNumber(int amount)
	{
		return amount < 1000 ? String.valueOf(amount) : amount / 1000 + "K";
	}
}
