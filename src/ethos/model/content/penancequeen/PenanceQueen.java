package ethos.model.content.penancequeen;

import ethos.Config;
import ethos.Server;
import ethos.event.CycleEvent;
import ethos.event.CycleEventContainer;
import ethos.event.CycleEventHandler;
import ethos.model.minigames.inferno.InfernoWave;
import ethos.model.players.Player;
import ethos.world.objects.GlobalObject;

public class PenanceQueen {

	private static Player player;
	
	private static final int START_X=3055;
	private static final int START_Y=3187;
	
	public PenanceQueen(Player player) {
		this.player = player;
	}
	
	public static void enterPenanceRoom() {
		CycleEventHandler.getSingleton().addEvent(player, new CycleEvent() {
			@Override
			public void execute(CycleEventContainer container) {
				if (container.getOwner() == null || player == null || player.isDead) {
					container.stop();
					return;
				}
				int instanceHeight = player.getIndex()*4;
				
				int cycle = container.getTotalTicks();
				
				if(cycle == 1) {
					player.getPA().startTeleport2(3050, 5200, instanceHeight);
					player.sendMessage("@red@Goodluck the queen will spawn in 5 seconds! Enter Exit when done!");
					player.sendMessage("@red@You have 10 minutes to kill her before the instance closes!");
				}
				if(cycle ==15) {
					Server.npcHandler.spawnNpc(player, 5775, 3053, 5195, instanceHeight, 0, 1200, 251, 1000, 1000, true, false);
				}
				if(cycle == 600) {
					Server.npcHandler.kill(5775, instanceHeight);
					
					container.stop();
				}
				container.stop();
			}
		}, 1);
	}
	public static void enterJadsRoom() {
		CycleEventHandler.getSingleton().addEvent(player, new CycleEvent() {
			@Override
			public void execute(CycleEventContainer container) {
				if (container.getOwner() == null || player == null || player.isDead) {
					container.stop();
					return;
				}
				int instanceHeight = player.getIndex()*4;
				
				int cycle = container.getTotalTicks();
				
				if(cycle == 1) {
					player.getPA().startTeleport2(3017, 5235, instanceHeight);
					player.sendMessage("@red@Goodluck the 2 jads will spawn in 5 seconds! Enter Exit when done!");
					player.sendMessage("@red@You have 10 minutes to kill them before the instance closes!");
				}
				if(cycle == 15) {
					Server.npcHandler.spawnNpc(player, 3127, 3028, 5238, instanceHeight, 0, 1200, 251, 1000, 1000, true, false);
					Server.npcHandler.spawnNpc(player, 3127, 3029, 5228, instanceHeight, 0, 1200, 251, 1000, 1000, true, false);
				}
				if(cycle == 600) {
					Server.npcHandler.kill(3127, instanceHeight);
					Server.npcHandler.kill(3127, instanceHeight);
					container.stop();
				}
			}
		}, 1);
	}
	public static void leaveRoom() {
		player.getPA().movePlayer(Config.EDGEVILLE_X, Config.EDGEVILLE_Y);
	}
}
