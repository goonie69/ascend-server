
package ethos.model.content.grandexchange;

import ethos.Config;
import ethos.Server;
import ethos.event.CycleEvent;
import ethos.event.CycleEventContainer;
import ethos.event.CycleEventHandler;
import ethos.model.items.Item;
import ethos.model.items.ItemAssistant;
import ethos.model.items.ItemDefinition;
import ethos.model.players.Player;
import ethos.model.players.PlayerSave;
import ethos.model.shops.ShopAssistant;
import ethos.util.Misc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * 
 *   [MENTION=52317]auth[/MENTION]or Alex(TheLife)
 */

public class GrandExchange {
	
	/**
	 * Static integers
	 */
	public static int offers = 100000,
			totalOffers = 0;
	
	/**
	 * []Integers
	 */
	public int Slots[] = new int[7];
	public int SlotType[] = new int[7];
	
	/**
	 * Integers
	 */
			
	public int selectedItemId = 0,
				selectedAmount = 0,
				selectedPrice = 0,
			    selectedSlot = 0,
			    itemRecieved = 0,
			    itemAmountRecieved = 0,
			    firstItemStacked,
			    secondItemStacked;
			
	/**
	 * Static booleans
	 */
	public static boolean loading = false;
	
	/**
	 * Booleans
	 */
	public boolean toHigh = false,
			recievedMessage = false,
			stillSearching = false;

	/**
	 * Sellers
	 */
	public static Sellers sellers[] = new Sellers[offers];
	
	/**
	 * Buyers
	 */
	public static Buyers buyers[] = new Buyers[offers];
	
	/**
	 * Initializing Player c
	 */
	private Player c;
	
	/**
	 * Setting c
	 */
    public GrandExchange (Player c) {
    	this.c = c;
    }
    
    /**
     * Setting it for Server.java
     */
	public GrandExchange() {
		
	}
	
	/**
	 * Send update to player
	 */
	public void sendUniversal(String name) {
		for (int i = 0; i < Config.MAX_PLAYERS; i++) {
			if (Server.playerHandler.players[i] != null) {
				if (Server.playerHandler.players[i].playerName.equalsIgnoreCase(name)) {
					Player c2 = (Player)Server.playerHandler.players[i];
					if(c2.GE().recievedMessage != true) {
					c2.sendMessage("One or more of your Grand Exchange offers have been updated.");
					}
					c2.GE().recievedMessage = true;
					if(c2.getInterfaceOpen() == 54700 || c2.getInterfaceOpen() == 53700) {
						final Player c3 = c2;
					CycleEventHandler.getSingleton().addEvent(c, new CycleEvent() {
						public void execute(CycleEventContainer container) {
							container.stop();
							}

					   @Override
						public void stop() {
							c3.GE().openCollect(c3.GE().selectedSlot, false);
						}
					}, 1);
					
					} else if(c2.getInterfaceOpen() == 24500) {
						final Player c3 = c2;
						CycleEventHandler.getSingleton().addEvent(c, new CycleEvent() {
							public void execute(CycleEventContainer container) {
								container.stop();
								}

						   @Override
							public void stop() {
								c3.GE().openGrandExchange(false);
							}
						}, 1);
					} else {
						c2.GE().openGrandExchange(false);
					}
				}	
			}			
		}
	}
	
	/**
	 * Send if it has updated
	 */
	public void sendUpdate(String name) {
		boolean rM = false;
		for(int i = 0; i < offers; i++) {
			if(sellers[i] != null && sellers[i].owner.equalsIgnoreCase(name)) {
				if(sellers[i].updated == true)
					rM = true;
			} else if(buyers[i] != null && buyers[i].owner.equalsIgnoreCase(name)) {
				if(buyers[i].updated == true)
					rM = true;
			}
		}
		if(rM == true) {
			c.sendMessage("You have items from the Grand Exchange waiting in your collection box.");
		}
	}
    
	/**
	 * Load Sellers
	 */
    public void loadSellers() {
		BufferedReader File = null;
    	boolean found = false;
		for(int i = 0; i < offers; i++) {
			found = false;
			try {
				File = new BufferedReader(new FileReader("./Data/GrandExchange/Sellers/"+i+".txt"));
				try {
				File.close();
				} catch(IOException o) {
					
				}
				found = true;
			} catch(FileNotFoundException e) {
				found = false;
				continue;
			}
			if(found == true) {
				totalOffers++;
				Sellers s = new Sellers(i);
				sellers[i] = s;
				loadOffer(s.id, "Sell");			
				}
		}
    }
    
	/**
	 * Load Buyers
	 */
    public void loadBuyers() {
		BufferedReader File = null;
    	boolean found = false;
		for(int i = 0; i < offers; i++) {
			found = false;
			try {
				File = new BufferedReader(new FileReader("./Data/GrandExchange/Buyers/"+i+".txt"));
				try {
				File.close();
				} catch(IOException o) {
					
				}
				found = true;
			} catch(FileNotFoundException e) {
				found = false;
				continue;
			}
			if(found == true) {
				totalOffers++;
				Buyers b = new Buyers(i);
				buyers[i] = b;
				loadOffer(b.id, "Buy");
			}
		}
    }
    
	/**
	 * Makes a new offer
	 */
    
	public int newOffer(String type, int itemId, int amount, int updatedAmount, int price, int percentage, String owner, boolean completed, int slot) {
		if(loading == true) {
			return -1;
		}
		int id = -1;
		if(type == "Sell") {
			for (int a = 1; a < offers; a++) {
				if (sellers[a] == null) {
					id = a;
					break;
				}
			}
			if(id == -1) {
				c.sendMessage("To many offers, please try again later.");
				return id;
			}
			Sellers s = new Sellers(id);
			s.itemId = itemId;
			s.amount = amount;
			s.updatedAmount = updatedAmount;
			s.price = price;
			s.percentage = percentage;
			s.owner = owner;
			s.slot = slot;
			s.updated = false;
			Slots[slot] = id;
			SlotType[slot] = 1;
			sellers[id] = s;
			saveOffer(id, type);
		} else if(type == "Buy") {
			for (int a = 1; a < offers; a++) {
				if (buyers[a] == null) {
					id = a;
					break;
				}
			}
			if(id == -1) {
				c.sendMessage("To many offers, please try again later.");
				return id;
			}
			Buyers b = new Buyers(id);
			b.itemId = itemId;
			b.amount = amount;
			b.updatedAmount = updatedAmount;
			b.price = price;
			b.percentage = percentage;
			b.owner = owner;
			b.slot = slot;
			b.updated = false;
			Slots[slot] = id;
			SlotType[slot] = 2;
			buyers[id] = b;
			saveOffer(id, type);
		}
		return id;
	}
	
	/**
	 * Check for items
	 */
    public int firstItemStacked(int itemId) {
    	firstItemStacked = 0;
		 if(ItemDefinition.forId(itemId+1).isNoteable()) {
			    itemId++;
		 }
		for (int i = 0; i < c.playerItems.length; i++)  {
			if (c.playerItems[i] == itemId+1)
					firstItemStacked = c.playerItemsN[i];
		   }
		return itemId;
    }
    
    public int secondItemStacked(int itemId) {
    	secondItemStacked = 0;
		for (int i = 0; i < c.playerItems.length; i++)  {
			if (c.playerItems[i] == itemId+1)
				secondItemStacked++;
		   }
		return itemId;
    }
	
    public boolean removeGrandExchangeItems(int selectedA, int firstItemId, int secondItemId, int firstItemA, int secondItemA, long total) {
    	if(total < selectedA) {
    		c.sendMessage("You don't have that many.");
    		return false;
    	}
    	if(selectedA <= 0) {
    		return false;
    	}
    	if(total <= 0) {
    		return false;
    	}
    	if(selectedA >= secondItemA) {
        	if (c.getItems().playerHasItem(secondItemId, secondItemA)) {
        		c.getItems().deleteItem2(secondItemId, secondItemA);
        		selectedA -= secondItemA;
        	}
    	}
       	if(selectedA <= secondItemA) {
        	if (c.getItems().playerHasItem(secondItemId, selectedA)) {
        		c.getItems().deleteItem2(secondItemId, selectedA);
        		selectedA = 0;
        	}
    	}
    	if(selectedA >= firstItemA) {
        	if (c.getItems().playerHasItem(firstItemId, firstItemA)) {
        		c.getItems().deleteItem2(firstItemId, firstItemA);
        		selectedA -= firstItemA;
        	}
    	}
       	if(selectedA <= firstItemA) {
        	if (c.getItems().playerHasItem(firstItemId, selectedA)) {
        		c.getItems().deleteItem2(firstItemId, selectedA);
        		selectedA = 0;
        	}
    	}
       	if(selectedA > 0) {
       		return false;
       	}
       	return true;
    }
    
	/**
	 * Sell items method
	 */
    public void sellItems() {
    	int x = selectedAmount;
    	int y = firstItemStacked(selectedItemId);
    	int z = secondItemStacked(selectedItemId);
    	
    	long l = firstItemStacked;
    	long g = secondItemStacked;
    	
    	long o = l+g;
    	
    	if(removeGrandExchangeItems(x, y, z, firstItemStacked, secondItemStacked, o)) {
    		final int s = selectedSlot; int b = selectedSlot;
    		int i = newOffer("Sell", selectedItemId, selectedAmount, selectedAmount, selectedPrice, 0, c.playerName, false, b);
    		//c.getItems().deleteItem2(selectedItemId, selectedAmount);
    		if(i == -1) {
    			c.getItems().addItem(selectedItemId, selectedAmount);
    			c.sendMessage("To many offers, please try again later.");
    			return;
    		}
    		openGrandExchange(true);
    		c.sendConfig(4, b, 1, -1);
    		c.sendConfig(5, b, 1, -1);
    		int k = b*2; k += 24565;
    		c.getPA().sendFrame34(selectedItemId, 0, k, selectedAmount);
    		sellItems(i, c.playerName);
			CycleEventHandler.getSingleton().addEvent(c, new CycleEvent() {
				public void execute(CycleEventContainer container) {

					container.stop();
					}

			   @Override
				public void stop() {
					c.sendConfig(5, s, 3, -1);
				}
			}, 2);
    		PlayerSave.saveGame(c);
    	 }
    }
    
    /**
     * Buy items method
     */
    public void buyItems() {
    	if (c.getItems().playerHasItem(995, selectedAmount*selectedPrice)) {
    		final int s = selectedSlot; int b = selectedSlot;
    		c.getItems().deleteItem2(995, selectedAmount*selectedPrice);
    		int i = newOffer("Buy", selectedItemId, selectedAmount, selectedAmount, selectedPrice, 0, c.playerName, false, b);
    		if(i == -1) {
    			c.getItems().addItem(selectedItemId, selectedAmount);
    			c.sendMessage("To many offers, please try again later.");
    			return;
    		}
    		openGrandExchange(true);
    		c.sendConfig(4, b, 1, -1);
    		c.sendConfig(5, b, 2, -1);
    		int k = b*2; k += 24565;
    		c.getPA().sendFrame34(selectedItemId, 0, k, selectedAmount);
    		buyItems(i, c.playerName);
			CycleEventHandler.getSingleton().addEvent(c, new CycleEvent() {
				public void execute(CycleEventContainer container) {
					container.stop();
					}

			   @Override
				public void stop() {
					c.sendConfig(5, s, 4, -1);
				}
			}, 2);
		    PlayerSave.saveGame(c);
    	} else {
    		c.sendMessage("You don't have enough coins.");
    	}
    }
    
	/**
	 * Part of item selling
	 */
    public void sellItems(int s, String name) {
		for (int b = 1; b < offers; b++) {
			if (buyers[b] != null && sellers[s] != null) {
				if (buyers[b].itemId == sellers[s].itemId) {
					if(sellers[s].updatedAmount >= 1) {
						if(sellers[s].price <= buyers[b].price) {
							if (!sellers[s].completed && !buyers[b].completed) {
							if (!sellers[s].aborted && !buyers[b].aborted) {
								stillSearching = true;
							boolean done = false; done = false;
							if(done != true && sellers[s].updatedAmount > buyers[b].updatedAmount) {
								sellers[s].updatedAmount -= buyers[b].updatedAmount;
								sellers[s].percentage += buyers[b].updatedAmount;
								sellers[s].itemOneAmount += buyers[b].updatedAmount*sellers[s].price;
								sellers[s].itemOne = getMoneyStackId(sellers[s].itemOneAmount);
								sellers[s].total += buyers[b].updatedAmount;
								sellers[s].totalGp += buyers[b].updatedAmount*sellers[s].price;
								buyers[b].itemOne = buyers[b].itemId;
								buyers[b].itemOneAmount += buyers[b].updatedAmount;
								buyers[b].total += buyers[b].updatedAmount;
								buyers[b].totalGp += buyers[b].updatedAmount*sellers[s].price;
								int sa = buyers[b].updatedAmount*sellers[s].price;
								int ba = buyers[b].updatedAmount*buyers[b].price;
								buyers[b].itemTwoAmount += ba-sa;
								buyers[b].itemTwo = getMoneyStackId(buyers[b].itemTwoAmount);
								buyers[b].percentage = buyers[b].amount;
								buyers[b].updatedAmount = 0;
								buyers[b].completed = true;
								buyers[b].updated = true;
								sellers[s].updated = true;
								sendUniversal(sellers[s].owner);
								sendUniversal(buyers[b].owner);
								saveOffer(s, "Sell");
								saveOffer(b, "Buy");
								done = true;
							}
							if(done != true && sellers[s].updatedAmount == buyers[b].updatedAmount) {
								sellers[s].percentage = sellers[s].amount;
								buyers[b].percentage = buyers[b].amount;
								sellers[s].itemOneAmount += buyers[b].updatedAmount*sellers[s].price;
								sellers[s].itemOne = getMoneyStackId(sellers[s].itemOneAmount);
								buyers[b].itemOne = buyers[b].itemId;
								buyers[b].itemOneAmount += sellers[s].updatedAmount;
								sellers[s].total += buyers[b].updatedAmount;
								sellers[s].totalGp += buyers[b].updatedAmount*sellers[s].price;
								buyers[b].total += buyers[b].updatedAmount;
								buyers[b].totalGp += buyers[b].updatedAmount*sellers[s].price;
								int sa = sellers[s].updatedAmount*sellers[s].price;
								int ba = buyers[b].updatedAmount*buyers[b].price;
								buyers[b].itemTwoAmount += ba-sa;
								buyers[b].itemTwo = getMoneyStackId(buyers[b].itemTwoAmount);
								sellers[s].updatedAmount = 0;
								buyers[b].updatedAmount = 0;
								buyers[b].completed = true;
								sellers[s].completed = true;
								buyers[b].updated = true;
								sellers[s].updated = true;
								sendUniversal(sellers[s].owner);
								sendUniversal(buyers[b].owner);
								saveOffer(b, "Buy");
								saveOffer(s, "Sell");
								done = true;
							}
							if(done != true && buyers[b].updatedAmount > sellers[s].updatedAmount) {
								buyers[b].percentage += sellers[s].updatedAmount;
								sellers[s].percentage = sellers[s].amount;
								sellers[s].itemOneAmount += sellers[s].updatedAmount*sellers[s].price;
								sellers[s].itemOne = getMoneyStackId(sellers[s].itemOneAmount);
								buyers[b].itemOne = buyers[b].itemId;
								buyers[b].itemOneAmount += sellers[s].updatedAmount;
								int sa = buyers[b].price-sellers[s].price;
								int ba = sa*sellers[s].updatedAmount;
								buyers[b].itemTwoAmount += ba;
								buyers[b].itemTwo = getMoneyStackId(buyers[b].itemTwoAmount);
								sellers[s].total += buyers[b].updatedAmount;
								sellers[s].totalGp += buyers[b].updatedAmount*sellers[s].price;
								buyers[b].total += buyers[b].updatedAmount;
								buyers[b].totalGp += buyers[b].updatedAmount*sellers[s].price;
								buyers[b].updatedAmount -= sellers[s].updatedAmount;
								sellers[s].updatedAmount = 0;
								sellers[s].completed = true;
								buyers[b].updated = true;
								sellers[s].updated = true;
								sendUniversal(sellers[s].owner);
								sendUniversal(buyers[b].owner);
								saveOffer(b, "Buy");
								saveOffer(s, "Sell");
								done = true;
							}
							stillSearching = false;
							}
							}
						}
					}
				}
			}
		}	
    }
    
	/**
	 * Part of item buying
	 */
    public void buyItems(int b, String name) {
		for (int s = 1; s < offers; s++) {
			if (sellers[s] != null && buyers[b] != null) {
				if (sellers[s].itemId == buyers[b].itemId) {
					if(buyers[b].updatedAmount >= 1) {
						if(buyers[b].price >= sellers[s].price) {
							if (!sellers[s].completed && !buyers[b].completed) {
							if (!sellers[s].aborted && !buyers[b].aborted) {
								stillSearching = true;
								boolean done = false; done = false;
							if(done != true && buyers[b].updatedAmount > sellers[s].updatedAmount) {
								buyers[b].percentage += sellers[s].updatedAmount;
								sellers[s].percentage = sellers[s].amount;
								buyers[b].updatedAmount -= sellers[s].updatedAmount;
								sellers[s].itemOneAmount += sellers[s].updatedAmount*sellers[s].price;
								sellers[s].itemOne = getMoneyStackId(sellers[s].itemOneAmount);
								buyers[b].itemOne = buyers[b].itemId;
								buyers[b].itemOneAmount += sellers[s].updatedAmount;
								int sa = buyers[b].price-sellers[s].price;
								int ba = sa*sellers[s].updatedAmount;
								buyers[b].itemTwoAmount += ba;
								buyers[b].itemTwo = getMoneyStackId(buyers[b].itemTwoAmount);	
								sellers[s].total += sellers[s].updatedAmount;
								sellers[s].totalGp += sellers[s].updatedAmount*sellers[s].price;
								buyers[b].total += sellers[s].updatedAmount;
								buyers[b].totalGp += sellers[s].updatedAmount*sellers[s].price;
								sellers[s].updatedAmount = 0;
								sellers[s].completed = true;
								buyers[b].updated = true;
								sellers[s].updated = true;
								sendUniversal(sellers[s].owner);
								sendUniversal(buyers[b].owner);
								saveOffer(s, "Sell");
								saveOffer(b, "Buy");
								done = true;
							}
							if(done != true && buyers[b].updatedAmount == sellers[s].updatedAmount) {
								sellers[s].percentage = sellers[s].amount;
								buyers[b].percentage = buyers[b].amount;
								sellers[s].itemOneAmount += buyers[b].updatedAmount*sellers[s].price;
								sellers[s].itemOne = getMoneyStackId(sellers[s].itemOneAmount);
								buyers[b].itemOne = buyers[b].itemId;
								buyers[b].itemOneAmount += sellers[s].updatedAmount;
								int sa = sellers[s].updatedAmount*sellers[s].price;
								int ba = buyers[b].updatedAmount*buyers[b].price;
								buyers[b].itemTwoAmount += ba-sa;
								buyers[b].itemTwo = getMoneyStackId(buyers[b].itemTwoAmount);
								sellers[s].total += buyers[b].updatedAmount;
								sellers[s].totalGp += buyers[b].updatedAmount*sellers[s].price;
								buyers[b].total += buyers[b].updatedAmount;
								sellers[s].totalGp += buyers[b].updatedAmount*sellers[s].price;
								buyers[b].updatedAmount = 0;
								sellers[s].updatedAmount = 0;
								buyers[b].completed = true;
								sellers[s].completed = true;
								buyers[b].updated = true;
								sellers[s].updated = true;
								sendUniversal(sellers[s].owner);
								sendUniversal(buyers[b].owner);
								saveOffer(b, "Buy");
								saveOffer(s, "Sell");
								done = true;
							}
							if(done != true && sellers[s].updatedAmount > buyers[b].updatedAmount) {
								sellers[s].percentage += buyers[b].updatedAmount;
								buyers[b].percentage = buyers[b].amount;
								sellers[s].itemOneAmount += buyers[b].updatedAmount*sellers[s].price;
								sellers[s].itemOne = getMoneyStackId(sellers[s].itemOneAmount);
								buyers[b].itemOne = buyers[b].itemId;
								buyers[b].itemOneAmount += buyers[b].updatedAmount;
								int sa = buyers[b].updatedAmount*sellers[s].price;
								int ba = buyers[b].updatedAmount*buyers[b].price;
								buyers[b].itemTwoAmount += ba-sa;
								buyers[b].itemTwo = getMoneyStackId(buyers[b].itemTwoAmount);
								sellers[s].total += buyers[b].updatedAmount;
								sellers[s].totalGp += buyers[b].updatedAmount*sellers[s].price;
								buyers[b].total += buyers[b].updatedAmount;
								buyers[b].totalGp += buyers[b].updatedAmount*sellers[s].price;
								sellers[s].updatedAmount -= buyers[b].updatedAmount;
								buyers[b].updatedAmount = 0;
								buyers[b].completed = true;
								buyers[b].updated = true;
								sellers[s].updated = true;
								sendUniversal(sellers[s].owner);
								sendUniversal(buyers[b].owner);
								saveOffer(b, "Buy");
								saveOffer(s, "Sell");
								done = true;
							}
							stillSearching = false;
							}
							}
						}
					}
				}
			}
		}	
    }
    
    /**
     * Gets the item id of the money stack
     */
    public int getMoneyStackId(int amount) {
		if(amount == 1) {
			return 995;
		} else if(amount == 2) {
			return 996;
		} else if(amount == 3) {
			return 997;
		} else if(amount == 4) {
			return 998;
		} else if(amount >= 5 && amount <= 24) {
			return 999;
		} else if(amount >= 25 && amount <= 99) {
			return 1000;
		} else if(amount >= 100 && amount <= 249) {
			return 1001;
		} else if(amount >= 250 && amount <= 999) {
			return 1002;
		} else if(amount >= 1000 && amount <= 9999) {
			return 1003;
		} else if(amount >= 10000) {
			return 1004;
		}
		return 995;
    }
    /**
     * M or K
     */
	private static String intToKOrMil(int j) {
		if(j < 0x186a0)
			return String.valueOf(j);
		if(j < 0x989680)
			return j / 1000 + "K";
		else
			return j / 0xf4240 + "M";
	}
    
    /**
     * Save offers
     */
	public void saveOffer(int id, String type) {
		BufferedWriter grandExchange = null;
		BufferedReader File = null;
		BufferedWriter fileW = null;
		if(type == "Sell") {
		try {
			try {
				File = new BufferedReader(new FileReader("./Data/GrandExchange/Sellers/"+id+".txt"));
				try {
				File.close();
				} catch(IOException o) {
					
				}
			} catch(FileNotFoundException e) {
				try {
				fileW = new BufferedWriter(new FileWriter("./Data/GrandExchange/Sellers/"+id+".txt"));
				try {
				fileW.close();
				} catch(IOException o) {
					
				}
				} catch(IOException a) {
					
				}
			}
			grandExchange = new BufferedWriter(new FileWriter("./Data/GrandExchange/Sellers/"+id+".txt"));
			grandExchange.write(Integer.toString(sellers[id].itemId), 0, Integer.toString(sellers[id].itemId).length());
			grandExchange.newLine();
			grandExchange.write(Integer.toString(sellers[id].amount), 0, Integer.toString(sellers[id].amount).length());
			grandExchange.newLine();
			grandExchange.write(Integer.toString(sellers[id].updatedAmount), 0, Integer.toString(sellers[id].updatedAmount).length());
			grandExchange.newLine();
			grandExchange.write(Integer.toString(sellers[id].price), 0, Integer.toString(sellers[id].price).length());
			grandExchange.newLine();
			grandExchange.write(Integer.toString(sellers[id].percentage), 0, Integer.toString(sellers[id].percentage).length());
			grandExchange.newLine();
			grandExchange.write(sellers[id].owner, 0, sellers[id].owner.length());
			grandExchange.newLine();
			if(sellers[id].completed == true) {
			grandExchange.write("1", 0, 1);
			} else {
			grandExchange.write("0", 0, 1);
			}
			grandExchange.newLine();
			grandExchange.write(Integer.toString(sellers[id].slot), 0, Integer.toString(sellers[id].slot).length());
			grandExchange.newLine();
			if(sellers[id].updated == true) {
			grandExchange.write("1", 0, 1);
			} else {
			grandExchange.write("0", 0, 1);
			}
			grandExchange.newLine();
			grandExchange.write(Integer.toString(sellers[id].itemOne), 0, Integer.toString(sellers[id].itemOne).length());
			grandExchange.newLine();
			grandExchange.write(Integer.toString(sellers[id].itemTwo), 0, Integer.toString(sellers[id].itemTwo).length());
			grandExchange.newLine();
			grandExchange.write(Integer.toString(sellers[id].itemOneAmount), 0, Integer.toString(sellers[id].itemOneAmount).length());
			grandExchange.newLine();
			grandExchange.write(Integer.toString(sellers[id].itemTwoAmount), 0, Integer.toString(sellers[id].itemTwoAmount).length());
			grandExchange.newLine();
			if(sellers[id].aborted == true) {
			grandExchange.write("1", 0, 1);
			} else {
			grandExchange.write("0", 0, 1);
			}
			grandExchange.newLine();
			grandExchange.write(Integer.toString(sellers[id].total), 0, Integer.toString(sellers[id].total).length());
			grandExchange.newLine();
			grandExchange.write(Integer.toString(sellers[id].totalGp), 0, Integer.toString(sellers[id].totalGp).length());
			grandExchange.close();
		} catch (IOException ioexception) {
		}
	} else if(type == "Buy") {
		try {
			try {
				File = new BufferedReader(new FileReader("./Data/GrandExchange/Buyers/"+id+".txt"));
				try {
				File.close();
				} catch(IOException o) {
					
				}
			} catch(FileNotFoundException e) {
				try {
				fileW = new BufferedWriter(new FileWriter("./Data/GrandExchange/Buyers/"+id+".txt"));
				try {
				fileW.close();
				} catch(IOException o) {
					
				}
				} catch(IOException a) {
					
				}
			}
			grandExchange = new BufferedWriter(new FileWriter("./Data/GrandExchange/Buyers/"+id+".txt"));
			grandExchange.write(Integer.toString(buyers[id].itemId), 0, Integer.toString(buyers[id].itemId).length());
			grandExchange.newLine();
			grandExchange.write(Integer.toString(buyers[id].amount), 0, Integer.toString(buyers[id].amount).length());
			grandExchange.newLine();
			grandExchange.write(Integer.toString(buyers[id].updatedAmount), 0, Integer.toString(buyers[id].updatedAmount).length());
			grandExchange.newLine();
			grandExchange.write(Integer.toString(buyers[id].price), 0, Integer.toString(buyers[id].price).length());
			grandExchange.newLine();
			grandExchange.write(Integer.toString(buyers[id].percentage), 0, Integer.toString(buyers[id].percentage).length());
			grandExchange.newLine();
			grandExchange.write(buyers[id].owner, 0, buyers[id].owner.length());
			grandExchange.newLine();
			if(buyers[id].completed == true) {
			grandExchange.write("1", 0, 1);
			} else {
			grandExchange.write("0", 0, 1);
			}
			grandExchange.newLine();
			grandExchange.write(Integer.toString(buyers[id].slot), 0, Integer.toString(buyers[id].slot).length());
			grandExchange.newLine();
			if(buyers[id].updated == true) {
			grandExchange.write("1", 0, 1);
			} else {
			grandExchange.write("0", 0, 1);
			}
			grandExchange.newLine();
			grandExchange.write(Integer.toString(buyers[id].itemOne), 0, Integer.toString(buyers[id].itemOne).length());
			grandExchange.newLine();
			grandExchange.write(Integer.toString(buyers[id].itemTwo), 0, Integer.toString(buyers[id].itemTwo).length());
			grandExchange.newLine();
			grandExchange.write(Integer.toString(buyers[id].itemOneAmount), 0, Integer.toString(buyers[id].itemOneAmount).length());
			grandExchange.newLine();
			grandExchange.write(Integer.toString(buyers[id].itemTwoAmount), 0, Integer.toString(buyers[id].itemTwoAmount).length());
			grandExchange.newLine();
			if(buyers[id].aborted == true) {
			grandExchange.write("1", 0, 1);
			} else {
			grandExchange.write("0", 0, 1);
			}
			grandExchange.newLine();
			grandExchange.write(Integer.toString(buyers[id].total), 0, Integer.toString(buyers[id].total).length());
			grandExchange.newLine();
			grandExchange.write(Integer.toString(buyers[id].totalGp), 0, Integer.toString(buyers[id].totalGp).length());
			grandExchange.close();
		} catch (IOException ioexception) {
		}	

	}
}
	
	/**
	 * Load offer
	 */
	public void loadOffer(int id, String type) {
		String s = "";
		int i = 1;
		try {
			BufferedReader bufferedreader = null;
			if(type == "Sell") {
			bufferedreader = new BufferedReader(new FileReader(
					"./Data/GrandExchange/Sellers/"+id+".txt"));
			} else {
			bufferedreader = new BufferedReader(new FileReader(
					"./Data/GrandExchange/Buyers/"+id+".txt"));
			}
			for (String s1 = bufferedreader.readLine(); s1 != null; s1 = bufferedreader
					.readLine()) {
				s1 = s1.trim();
				if(type == "Sell") {
				if(i == 1) {
					sellers[id].itemId = Integer.parseInt(s1);
				} else if(i == 2) {
					sellers[id].amount = Integer.parseInt(s1);
				} else if(i == 3) {
					sellers[id].updatedAmount = Integer.parseInt(s1);
				} else if(i == 4) {
					sellers[id].price = Integer.parseInt(s1);
				} else if(i == 5) {
					sellers[id].percentage = Integer.parseInt(s1);
				} else if(i == 6) {
					sellers[id].owner = s1; 
				} else if(i == 7) {
				int bol = Integer.parseInt(s1);
				if(bol == 1)
					sellers[id].completed = true;
				else
					sellers[id].completed = false;
				} else if(i == 8) {
					sellers[id].slot = Integer.parseInt(s1);
				} else if(i == 9) {
					int bol = Integer.parseInt(s1);
					if(bol == 1)
						sellers[id].updated = true;
					else
						sellers[id].updated = false;
				} else if(i == 10) {
					sellers[id].itemOne = Integer.parseInt(s1);
				} else if(i == 11) {
					sellers[id].itemTwo = Integer.parseInt(s1);
				} else if(i == 12) {
					sellers[id].itemOneAmount = Integer.parseInt(s1);
				} else if(i == 13) {
					sellers[id].itemTwoAmount = Integer.parseInt(s1);
				} else if(i == 14) {
					int bol = Integer.parseInt(s1);
					if(bol == 1)
						sellers[id].aborted = true;
					else
						sellers[id].aborted = false;
				} else if(i == 15) {
					sellers[id].total = Integer.parseInt(s1);
				} else if(i == 16) {
					sellers[id].totalGp = Integer.parseInt(s1);
				}
				} else {
				if(i == 1) {
					buyers[id].itemId = Integer.parseInt(s1);
				} else if(i == 2) {
					buyers[id].amount = Integer.parseInt(s1);
				} else if(i == 3) {
					buyers[id].updatedAmount = Integer.parseInt(s1);
				} else if(i == 4) {
					buyers[id].price = Integer.parseInt(s1);
				} else if(i == 5) {
					buyers[id].percentage = Integer.parseInt(s1);
				} else if(i == 6) {
					buyers[id].owner = s1; 
				} else if(i == 7) {
				int bol = Integer.parseInt(s1);
				if(bol == 1)
					buyers[id].completed = true;
				else
					buyers[id].completed = false;
				} else if(i == 8) {
					buyers[id].slot = Integer.parseInt(s1);
				} else if(i == 9) {
					int bol = Integer.parseInt(s1);
					if(bol == 1)
						buyers[id].updated = true;
					else
						buyers[id].updated = false;
				} else if(i == 10) {
					buyers[id].itemOne = Integer.parseInt(s1);
				} else if(i == 11) {
					buyers[id].itemTwo = Integer.parseInt(s1);
				} else if(i == 12) {
					buyers[id].itemOneAmount = Integer.parseInt(s1);
				} else if(i == 13) {
					buyers[id].itemTwoAmount = Integer.parseInt(s1);
				} else if(i == 14) {
					int bol = Integer.parseInt(s1);
					if(bol == 1)
						buyers[id].aborted = true;
					else
						buyers[id].aborted = false;
				} else if(i == 15) {
					buyers[id].total = Integer.parseInt(s1);
				} else if(i == 16) {
					buyers[id].totalGp = Integer.parseInt(s1);
				}
				}
				i++;
			}

			bufferedreader.close();
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}
	
	/**
	 * Updates percentage bar
	 */
	public void updateBar(String color, String type, int i, int slot, Player c2) {
		if(color == "Red") {
			c2.sendConfig(3, slot, 1, -1);
			c2.sendConfig(2, slot, 100, -1);
		}
		if(color == "Green") {	
			double p; int col;
			if(type == "Sell" && sellers[i] != null) {
				p = ((double)sellers[i].percentage / sellers[i].amount) * 100;
				col = (int)p;
				c2.sendConfig(3, slot, 2, -1);
				c2.sendConfig(1, slot, 2, -1);
				c2.sendConfig(2, slot, col, -1);
			} else if(type == "Buy" && buyers[i] != null) {
				p = ((double)buyers[i].percentage / buyers[i].amount) * 100;
				col = (int)p;
				c2.sendConfig(3, slot, 2, -1);
				c2.sendConfig(1, slot, 2, -1);
				c2.sendConfig(2, slot, col, -1);
			}
		}

	}
	
	/**
	 * Button click
	 */
	public void buttonClick(int buttonId) {
		switch(buttonId) {
		case 95185:
			selectedSlot = 1;
			openInterface("Buy");
			break;
		case 95191:
			selectedSlot = 1;
			openInterface("Sell");
			break;
		case 95203:
			selectedSlot = 2;
			openInterface("Buy");
			break;
		case 95206:
			selectedSlot = 2;
			openInterface("Sell");
			break;
		case 95194:
			selectedSlot = 3;
			openInterface("Buy");
			break;
		case 95209:
			selectedSlot = 3;
			openInterface("Sell");
			break;
		case 95188:
			selectedSlot = 4;
			openInterface("Buy");
			break;
		case 95212:
			selectedSlot = 4;
			openInterface("Sell");
			break;
		case 95197:
			selectedSlot = 5;
			openInterface("Buy");
			break;
		case 95215:
			selectedSlot = 5;
			openInterface("Sell");
			break;
		case 95200:
			selectedSlot = 6;
			openInterface("Buy");
			break;
		case 95218:
			selectedSlot = 6;
			openInterface("Sell");
			break;
		case 96078:
			c.sendConfig(6, -1, -1, -1);
			break;
		case 96174:
			if(selectedItemId == 0) {
				c.sendMessage("You must choose an item first.");
				return;
			}
			completeOffer("Sell");
			break;
		case 96074:
			if(selectedItemId == 0) {
				c.sendMessage("You must choose an item first.");
				return;
			}
			completeOffer("Buy");
			break;
		case 96082:
		case 96182:
			c.getPA().showInterface(24500);
			break;
		case 96030:
		case 96130:
			if(selectedItemId == 0) {
				c.sendMessage("You must choose an item first.");
				return;
			}
			if(selectedAmount != 0) {
				selectedAmount--;
				updateGE(selectedItemId, selectedPrice);
			}
			break;
		case 96034:
		case 96134:
			if(selectedItemId == 0) {
				c.sendMessage("You must choose an item first.");
				return;
			}
			if(selectedAmount != 2147483647) {
				selectedAmount++;
				updateGE(selectedItemId, selectedPrice);
			}
			break;
		case 96038:
		case 96138:
			if(selectedItemId == 0) {
				c.sendMessage("You must choose an item first.");
				return;
			}
			if(selectedAmount != 2147483647) {
				selectedAmount++;
				updateGE(selectedItemId, selectedPrice);
			}
			break;
		case 96042:
		case 96142:
			if(selectedItemId == 0) {
				c.sendMessage("You must choose an item first.");
				return;
			}
			 long a = selectedAmount;
			 long total = a+10;
			if(total < 2147483647) {
				selectedAmount += 10;
				updateGE(selectedItemId, selectedPrice);
			} else {
				selectedAmount = 2147483647;
				updateGE(selectedItemId, selectedPrice);
			}
			break;
		case 96046:
		case 96146:
			if(selectedItemId == 0) {
				c.sendMessage("You must choose an item first.");
				return;
			}
			 long l = selectedAmount;
			 long t = l+100;
			if(t < 2147483647) {
				selectedAmount += 100;
				updateGE(selectedItemId, selectedPrice);
			} else {
				selectedAmount = 2147483647;
				updateGE(selectedItemId, selectedPrice);
			}
			break;
		case 96050:
		case 96150:
			if(selectedItemId == 0) {
				c.sendMessage("You must choose an item first.");
				return;
			}
			 long h = selectedAmount;
			 long d = h+1000;
			if(d < 2147483647) {
				selectedAmount += 1000;
				updateGE(selectedItemId, selectedPrice);
			} else {
				selectedAmount = 2147483647;
				updateGE(selectedItemId, selectedPrice);
			}
			break;
		case 96058:
		case 96158:
			if(selectedItemId == 0) {
				c.sendMessage("You must choose an item first.");
				return;
			}
			if(selectedPrice != 1) {
				selectedPrice *= 0.95;
				updateGE(selectedItemId, selectedPrice);
				if(selectedPrice < 1) {
					selectedPrice = 1;
					updateGE(selectedItemId, selectedPrice);
				}
			}
			break;	
		case 96070:
		case 96170:
			if(selectedItemId == 0) {
				c.sendMessage("You must choose an item first.");
				return;
			}
			 long k = selectedPrice; k *= 1.05;
			if(k <= 2147483647) {
				selectedPrice *= 1.05;
				if(selectedPrice < 21)
					selectedPrice++;
				updateGE(selectedItemId, selectedPrice);
			} else {
				selectedPrice = 2147483647;
				updateGE(selectedItemId, selectedPrice);
			}
			break;
		case 96062:
		case 96162:
			if(selectedItemId == 0) {
				c.sendMessage("You must choose an item first.");
				return;
			}
				selectedPrice = ShopAssistant.getItemShopValue(selectedItemId);
				updateGE(selectedItemId, selectedPrice);
			break;
		case 96086:
		case 96186:
			if(selectedItemId == 0) {
				c.sendMessage("You must choose an item first.");
				return;
			}
			selectedPrice--;
			if(selectedPrice == 0)
				selectedPrice = 1;
			updateGE(selectedItemId, selectedPrice);
			break;
		case 96089:
		case 96189:
			if(selectedItemId == 0) {
				c.sendMessage("You must choose an item first.");
				return;
			}
			if(selectedPrice != 2147483647) {
				selectedPrice++;
				updateGE(selectedItemId, selectedPrice);
			} else {
				selectedPrice = 2147483647;
			}
			break;
		case 95223:
			selectedSlot = 1;
			openCollect(selectedSlot, true);
			break;
		case 95227:
			selectedSlot = 2;
			openCollect(selectedSlot, true);
			break;
		case 95231:
			selectedSlot = 3;
			openCollect(selectedSlot, true);
			break;
		case 95235:
			selectedSlot = 4;
			openCollect(selectedSlot, true);
			break;
		case 95239:
			selectedSlot = 5;
			openCollect(selectedSlot, true);
			break;
		case 95243:
			selectedSlot = 6;
			openCollect(selectedSlot, true);
			break;
		case 213230:
		case 209254:
			openGrandExchange(true);
			break;
		case 95221:
			abortOffer(1, true);
			break;
		case 95225:
			abortOffer(2, true);
			break;
		case 95229:
			abortOffer(3, true);
			break;
		case 95233:
			abortOffer(4, true);
			break;
		case 95237:
			abortOffer(5, true);
			break;
		case 95241:
			abortOffer(6, true);
			break;
		case 214016:
		case 210040:
			abortOffer(selectedSlot, true);
			openCollect(selectedSlot, false);
			break;
		}
	}
	
	/**
	 * Complete offer
	 */
	public void completeOffer(String type) {
		if(Slots[selectedSlot] > 0) {
			return;
		}
		if(toHigh) {
			c.sendMessage("The offer you made has to high of a price.");
			return;
		}
		if(selectedAmount == 0) {
			c.sendMessage("You must buy more than one.");
			return;
		}
		if(type == "Sell") {
			sellItems();
		} else if(type == "Buy") {
			buyItems();
		}
		
	}
	
	
	/**
	 * Update Grand Exchange
	 */
	public void updateGE(int id, int price) {
		if(price == 0) {
			price = 1;
			selectedPrice = 1;
		}
		 c.getPA().sendFrame126(Misc.format(price)+" bm", 24672);
		 c.getPA().sendFrame126(Misc.format(price)+" bm", 24772);
		 long p = price; long s = selectedAmount;
		 long total = s*p;
		 if(total <= 2147483647) {
			 c.getPA().sendFrame126(Misc.format(price*selectedAmount)+" bm", 24673);
		 	 c.getPA().sendFrame126(Misc.format(price*selectedAmount)+" bm", 24773);
		 	 toHigh = false;
		 } else {
			 c.getPA().sendFrame126("Too high!", 24673);
			 c.getPA().sendFrame126("Too high!", 24773);
			 toHigh = true;
		 }
		 c.getPA().sendFrame126(Misc.format(selectedAmount)  +"", 24671);
		 c.getPA().sendFrame126(Misc.format(selectedAmount)  +"", 24771);
		 c.getPA().sendFrame126(Misc.format(c.getShops().getSpecialItemValue(id))+"", 24682);
		 c.getPA().sendFrame126(Misc.format(c.getShops().getSpecialItemValue(id))+"", 24782);
		 if(id > 0) {
				c.getPA().sendFrame126(""+c.getItems().getItemName(id), 24669);
				c.getPA().sendFrame126("It's a "+c.getItems().getItemName(id), 24670);
				c.getPA().sendFrame126(""+c.getItems().getItemName(id), 24769);
				c.getPA().sendFrame126("It's a "+c.getItems().getItemName(id), 24770);
				c.getPA().sendFrame34(id, 0, 24780, 1);
				c.getPA().sendFrame34(id, 0, 24680, 1);
		 } else {
			 c.getPA().sendFrame34(-1, 0, 24780, 1);
			 c.getPA().sendFrame34(-1, 0, 24680, 1); 
		 }
	}
	
	/**
	 * Open the collection interface
	 */
	public void openCollect(int id, boolean open) {
		if(sellers[Slots[id]] != null && sellers[Slots[id]].owner.equalsIgnoreCase(c.playerName) && SlotType[id] == 1) {
			 long p = sellers[Slots[id]].price; long a = sellers[Slots[id]].amount;
			 long total = a*p;
			 if(total <= 2147483647) {
				 c.getPA().sendFrame126(Misc.format(sellers[Slots[id]].amount)+"", 54771);
				 c.getPA().sendFrame126(Misc.format(sellers[Slots[id]].price)+" gp", 54772);
				 c.getPA().sendFrame126(Misc.format(sellers[Slots[id]].price*sellers[Slots[id]].amount)+" gp", 54773);
				if(sellers[Slots[id]].itemId > 0) {
					c.getPA().sendFrame34(sellers[Slots[id]].itemId, 0, 54780, 1);
				} else {
					return;
				}
				 if(sellers[Slots[id]].itemOne > 0 && sellers[Slots[id]].itemOneAmount > 0) {					 
					 c.getPA().sendFrame34(sellers[Slots[id]].itemOne, 0, 54781, 1);
					 if(sellers[Slots[id]].itemOneAmount > 1) {
						 c.getPA().sendFrame126(intToKOrMil(sellers[Slots[id]].itemOneAmount)+"", 54784);
					 } else {
						 c.getPA().sendFrame126("", 54784); 
					 }
				 } else {
					 c.getPA().sendFrame34(-1, 0, 54781, 1);
					 c.getPA().sendFrame126("", 54784); 
				 }
				 if(sellers[Slots[id]].itemTwo > 0 && sellers[Slots[id]].itemTwoAmount > 0) {
					 c.getPA().sendFrame34(sellers[Slots[id]].itemTwo, 0, 54782, 1);
					 if(sellers[Slots[id]].itemTwoAmount > 1) {
						 c.getPA().sendFrame126(intToKOrMil(sellers[Slots[id]].itemTwoAmount)+"", 54785);
					 } else {
						 c.getPA().sendFrame126("", 54785); 
					 }
				 } else {
					 c.getPA().sendFrame34(-1, 0, 54782, 1);
					 c.getPA().sendFrame126("", 54785); 
				 }
				 c.getPA().sendFrame126("@gec@You sold a total of @gea@"+sellers[Slots[id]].total, 54788);
				 c.getPA().sendFrame126("@gec@for a total price of @gea@"+sellers[Slots[id]].totalGp+"@gec@ gp.", 54789);
				 c.getPA().sendFrame126(Misc.format(c.getShops().getItemShopValue(sellers[Slots[id]].itemId))+"", 54787);
				 c.getPA().sendFrame126(""+c.getItems().getItemName(sellers[Slots[id]].itemId), 53769);
				 c.getPA().sendFrame126("It's a "+c.getItems().getItemName(sellers[Slots[id]].itemId), 53770);
				 c.sendConfig(4, selectedSlot, 2, -1);
				 if(open) {
				 c.getPA().showInterface(54700);
				 }
			 } else {
			 }
		} else if(buyers[Slots[id]] != null && buyers[Slots[id]].owner.equalsIgnoreCase(c.playerName) && SlotType[id] == 2) {
			 long p = buyers[Slots[id]].price; long a = buyers[Slots[id]].amount;
			 long total = a*p;
			 if(total <= 2147483647) {
				 c.getPA().sendFrame126(Misc.format(buyers[Slots[id]].amount)+"", 53771);
				 c.getPA().sendFrame126(Misc.format(buyers[Slots[id]].price)+" gp", 53772);
				 c.getPA().sendFrame126(Misc.format(buyers[Slots[id]].price*buyers[Slots[id]].amount)+" gp", 53773);
				if(buyers[Slots[id]].itemId > 0) {
					c.getPA().sendFrame34(buyers[Slots[id]].itemId, 0, 53780, 1);
				} else {
					return;
				}
				 if(buyers[Slots[id]].itemOne > 0 && buyers[Slots[id]].itemOneAmount > 0) {					 
					 c.getPA().sendFrame34(buyers[Slots[id]].itemOne, 0, 53781, 1);
					 c.getPA().sendFrame126(buyers[Slots[id]].itemOneAmount+"", 53784);
					 if(buyers[Slots[id]].itemOneAmount > 1) {
						 c.getPA().sendFrame126(intToKOrMil(buyers[Slots[id]].itemOneAmount)+"", 53784);
					 } else {
						 c.getPA().sendFrame126("", 53784); 
					 }
				 } else {
					 c.getPA().sendFrame34(-1, 0, 53781, 1);
					 c.getPA().sendFrame126("", 53784); 
				 }
				 if(buyers[Slots[id]].itemTwo > 0 && buyers[Slots[id]].itemTwoAmount > 0) {
					 c.getPA().sendFrame34(buyers[Slots[id]].itemTwo, 0, 53782, 1);
					 if(buyers[Slots[id]].itemTwoAmount > 1) {
						 c.getPA().sendFrame126(intToKOrMil(buyers[Slots[id]].itemTwoAmount)+"", 53785);
					 } else {
						 c.getPA().sendFrame126("", 53785); 
					 }
				 } else {
					 c.getPA().sendFrame34(-1, 0, 53782, 1);
					 c.getPA().sendFrame126("", 53785); 
				 }
				 c.getPA().sendFrame126("@gec@You bought a total of @gea@"+buyers[Slots[id]].total, 53788);
				 c.getPA().sendFrame126("@gec@for a total price of @gea@"+buyers[Slots[id]].totalGp+"@gec@ gp.", 53789);
				 c.getPA().sendFrame126(Misc.format(c.getShops().getItemShopValue(buyers[Slots[id]].itemId))+"", 53787);
				 c.getPA().sendFrame126(""+c.getItems().getItemName(buyers[Slots[id]].itemId), 54769);
				 c.getPA().sendFrame126("It's a "+c.getItems().getItemName(buyers[Slots[id]].itemId), 54770);
				 c.sendConfig(4, selectedSlot, 2, -1);
				 if(open) {
				 c.getPA().showInterface(53700);
				 }
			 } else {
			 }
		}
	}
	
	/**
	 * Open main Grand Exchange interface
	 */
	public void openGrandExchange(boolean open) {
		for(int i = 1; i < Slots.length; i++) {
		if(Slots[i] != 0) {
			if(sellers[Slots[i]] != null && sellers[Slots[i]].owner.equalsIgnoreCase(c.playerName) && sellers[Slots[i]].slot == i) {
				if(sellers[Slots[i]].completed) {
					c.sendConfig(5, i, 5, -1);
				} else {
					c.sendConfig(5, i, 3, -1);
				}
				int k = i*2; k += 24565;
	    		c.getPA().sendFrame34(sellers[Slots[i]].itemId, 0, k, sellers[Slots[i]].amount);
				c.getPA().sendFrame126(""+c.getItems().getItemName(sellers[Slots[i]].itemId)+"", 32000+i);
				c.getPA().sendFrame126(Misc.format(sellers[Slots[i]].price)+" gp", 33000+i);
				if(sellers[Slots[i]].amount != 1) {
					c.getPA().sendFrame126(""+intToKOrMil(sellers[Slots[i]].amount), 33100+i);
				} else {
					c.getPA().sendFrame126("", 33100+i);
				}
	    		if(sellers[Slots[i]].aborted) {
	    			updateBar("Red", "Sell", Slots[i], i, c);
	    		} else {
	    			updateBar("Green", "Sell", Slots[i], i, c);
	    		}
			} else if(buyers[Slots[i]] != null && buyers[Slots[i]].owner.equalsIgnoreCase(c.playerName) && buyers[Slots[i]].slot == i) {
				if(buyers[Slots[i]].completed) {
					c.sendConfig(5, i, 6, -1);
				} else {
					c.sendConfig(5, i, 4, -1);
				}
				int k = i*2; k += 24565;
	    		c.getPA().sendFrame34(buyers[Slots[i]].itemId, 0, k, buyers[Slots[i]].amount);
				c.getItems();
				c.getPA().sendFrame126(""+ItemAssistant.getItemName(buyers[Slots[i]].itemId)+"", 32000+i);
				c.getPA().sendFrame126(Misc.format(buyers[Slots[i]].price)+" gp", 33000+i);
				if(buyers[Slots[i]].amount != 1) {
					c.getPA().sendFrame126(""+intToKOrMil(buyers[Slots[i]].amount), 33100+i);
				} else {
					c.getPA().sendFrame126("", 33100+i);
				}
	    		if(buyers[Slots[i]].aborted) {
	    			updateBar("Red", "Buy", Slots[i], i, c);
	    		} else {
	    			updateBar("Green", "Buy", Slots[i], i, c);
	    		}
			}
			} else {
				c.sendConfig(4, i, 3, -1);
	    		int k = i*2; k += 24565;
	    		c.getPA().sendFrame34(-1, 0, k, 1);
				c.getPA().sendFrame126("", 33000+i);
				c.getPA().sendFrame126("", 32000+i);
				c.getPA().sendFrame126("", 33100+i);
		}
	}
		if(open) {
			c.getPA().showInterface(24500);
			recievedMessage = false;
		}
	}
	
	/**
	 * Open Buy or Sell interface
	 */
	public void openInterface(String type) {
		if(Slots[selectedSlot] > 0) {
			return;
		}
		selectedItemId = 0;
		selectedAmount = 0;
		selectedPrice = 0;
		updateGE(-1, 1);
		c.getPA().sendFrame126("Choose an item to exchange", 24669);
		c.getPA().sendFrame126("Click the icon to the left to search for items.", 24670);
		c.getPA().sendFrame126("Choose an item to exchange", 24769);
		c.getPA().sendFrame126("Select an item from your invertory to sell.", 24770);
		c.getPA().sendFrame126("N/A", 24682);
		c.getPA().sendFrame126("N/A", 24782);
		//c.sendConfig(4, selectedSlot, 2, -1);
		if(type == "Sell") {
			c.getPA().showInterface(24700);
		} else if(type == "Buy") {
			c.getPA().showInterface(24600);
		}
	}
	
	/**
	 * Item collecting
	 */
	public void collectItem(int i, String type) {
		int itemId = 0;
		if(type == "Sell") {
			if(i == 1) {
				if(sellers[Slots[selectedSlot]] == null) {
					return;
				}
				itemId = sellers[Slots[selectedSlot]].itemOne;
				 if(ItemDefinition.forId(itemId).isNoteable())
					 itemId -= 1;
				 if(c.getItems().freeSlots() == 0) {
					 	c.sendMessage("You don't have enough inventory space.");
					 	return;
					 }
				 if(itemId >= 996 && itemId <= 1004) {
					 itemId = 995;
				 }
				 if(sellers[Slots[selectedSlot]].itemOneAmount <= 0) {
					 return;
				 }
				 if(c.getItems().freeSlots() > 0 && !ItemDefinition.forId(itemId).isStackable()) {
					 	c.getItems().addItem(itemId, sellers[Slots[selectedSlot]].itemOneAmount);
					 	sellers[Slots[selectedSlot]].itemOneAmount = 0;
					 }
				if(c.getItems().freeSlots() > 0 && !ItemDefinition.forId(itemId).isStackable()) {
					 if(ItemDefinition.forId(itemId+1).isNoteable()) {
						 itemId += 1;
					} else {
						 return;
					}
						c.getItems().addItem(itemId, sellers[Slots[selectedSlot]].itemOneAmount);
						sellers[Slots[selectedSlot]].itemOneAmount = 0;
					}
				boolean save = true;
				if(sellers[Slots[selectedSlot]].itemOneAmount == 0 && sellers[Slots[selectedSlot]].itemTwoAmount == 0 && sellers[Slots[selectedSlot]].updatedAmount != 0) {
					sellers[Slots[selectedSlot]].updated = false;
				}
				if(sellers[Slots[selectedSlot]].itemOneAmount == 0 && sellers[Slots[selectedSlot]].itemTwoAmount == 0 && sellers[Slots[selectedSlot]].updatedAmount == 0) {
					deleteFile("Data/GrandExchange/Sellers/"+Slots[selectedSlot]+"");
					sellers[Slots[selectedSlot]].itemId = 0;
					sellers[Slots[selectedSlot]].amount = 0;
					sellers[Slots[selectedSlot]].updatedAmount = 0;
					sellers[Slots[selectedSlot]].price = 0;
					sellers[Slots[selectedSlot]].percentage = 0;
					sellers[Slots[selectedSlot]].slot = 0;
					sellers[Slots[selectedSlot]].itemOne = 0;
					sellers[Slots[selectedSlot]].itemTwo = 0;
					sellers[Slots[selectedSlot]].itemOneAmount = 0;
					sellers[Slots[selectedSlot]].itemTwoAmount = 0;
					sellers[Slots[selectedSlot]].total = 0;
					sellers[Slots[selectedSlot]].totalGp = 0;
					sellers[Slots[selectedSlot]].aborted = false;
					sellers[Slots[selectedSlot]].completed = false;
					sellers[Slots[selectedSlot]].updated = false;
					sellers[Slots[selectedSlot]].owner = "";
					sellers[Slots[selectedSlot]] = null;
					Slots[selectedSlot] = 0;
					SlotType[selectedSlot] = 0;
					openGrandExchange(true);
					save = false;
				}
				if(save) {
				saveOffer(Slots[selectedSlot], "Sell");
				}
				recievedMessage = false;
				PlayerSave.saveGame(c);
				openCollect(selectedSlot, false);
			}
			if(i == 2) {
				if(sellers[Slots[selectedSlot]] == null) {
					return;
				}
				itemId = sellers[Slots[selectedSlot]].itemTwo;
				 if(ItemDefinition.forId(itemId).isNoteable())
						 itemId -= 1;
					 if(c.getItems().freeSlots() == 0) {
						 	c.sendMessage("You don't have enough inventory space.");
						 	return;
						 }
					 if(itemId >= 996 && itemId <= 1004) {
						 itemId = 995;
					 }
					 if(sellers[Slots[selectedSlot]].itemTwoAmount <= 0) {
						 return;
					 }
					 if(c.getItems().freeSlots() > 0 && ItemDefinition.forId(itemId).isStackable()) {
						 	c.getItems().addItem(itemId, sellers[Slots[selectedSlot]].itemTwoAmount);
						 	sellers[Slots[selectedSlot]].itemTwoAmount = 0;
						 }
					if(c.getItems().freeSlots() > 0 && !ItemDefinition.forId(itemId).isStackable()) {
						 if(ItemDefinition.forId(itemId+1).isNoteable()) {
							 itemId += 1;
						} else {
							 return;
						}
							c.getItems().addItem(itemId, sellers[Slots[selectedSlot]].itemTwoAmount);
							sellers[Slots[selectedSlot]].itemTwoAmount = 0;
						}
					boolean save = true;
					if(sellers[Slots[selectedSlot]].itemOneAmount == 0 && sellers[Slots[selectedSlot]].itemTwoAmount == 0 && sellers[Slots[selectedSlot]].updatedAmount != 0) {
						sellers[Slots[selectedSlot]].updated = false;
					}
					if(sellers[Slots[selectedSlot]].itemOneAmount == 0 && sellers[Slots[selectedSlot]].itemTwoAmount == 0 && sellers[Slots[selectedSlot]].updatedAmount == 0) {
						deleteFile("Data/GrandExchange/Sellers/"+Slots[selectedSlot]+"");
						sellers[Slots[selectedSlot]].id = 0;
						sellers[Slots[selectedSlot]].itemId = 0;
						sellers[Slots[selectedSlot]].amount = 0;
						sellers[Slots[selectedSlot]].updatedAmount = 0;
						sellers[Slots[selectedSlot]].price = 0;
						sellers[Slots[selectedSlot]].percentage = 0;
						sellers[Slots[selectedSlot]].slot = 0;
						sellers[Slots[selectedSlot]].total = 0;
						sellers[Slots[selectedSlot]].totalGp = 0;
						sellers[Slots[selectedSlot]].itemOne = 0;
						sellers[Slots[selectedSlot]].itemTwo = 0;
						sellers[Slots[selectedSlot]].itemOneAmount = 0;
						sellers[Slots[selectedSlot]].itemTwoAmount = 0;
						sellers[Slots[selectedSlot]].aborted = false;
						sellers[Slots[selectedSlot]].completed = false;
						sellers[Slots[selectedSlot]].updated = false;
						sellers[Slots[selectedSlot]].owner = "";
						sellers[Slots[selectedSlot]] = null;
						Slots[selectedSlot] = 0;
						SlotType[selectedSlot] = 0;
						openGrandExchange(true);
						save = false;
					}
					if(save) {
					saveOffer(Slots[selectedSlot], "Sell");
					}
					recievedMessage = false;
					PlayerSave.saveGame(c);
					openCollect(selectedSlot, false);
				}
		}
		if(type == "Buy") {
			if(i == 1) {
				if(buyers[Slots[selectedSlot]] == null) {
					return;
				}
				itemId = buyers[Slots[selectedSlot]].itemOne;
				 if(ItemDefinition.forId(itemId).isNoteable())
					 itemId -= 1;
				 if(c.getItems().freeSlots() == 0) {
					 	c.sendMessage("You don't have enough inventory space.");
					 	return;
					 }
				 if(itemId >= 996 && itemId <= 1004) {
					 itemId = 995;
				 }
				 if(buyers[Slots[selectedSlot]].itemOneAmount <= 0) {
					 return;
				 }
				 if(c.getItems().freeSlots() > 0 && ItemDefinition.forId(itemId).isStackable()) {
					 	c.getItems().addItem(itemId, buyers[Slots[selectedSlot]].itemOneAmount);
					 	buyers[Slots[selectedSlot]].itemOneAmount = 0;
					 }
				if(c.getItems().freeSlots() > 0 && !ItemDefinition.forId(itemId).isStackable()) {
					if(ItemDefinition.forId(itemId+1).isNoteable()) {
						 itemId += 1;
					} else {
						 return;
					}
						c.getItems().addItem(itemId, buyers[Slots[selectedSlot]].itemOneAmount);
						buyers[Slots[selectedSlot]].itemOneAmount = 0;
					}
				boolean save = true;
				if(buyers[Slots[selectedSlot]].itemOneAmount == 0 && buyers[Slots[selectedSlot]].itemTwoAmount == 0 && buyers[Slots[selectedSlot]].updatedAmount != 0) {
					buyers[Slots[selectedSlot]].updated = false;
				}
				if(buyers[Slots[selectedSlot]].itemOneAmount == 0 && buyers[Slots[selectedSlot]].itemTwoAmount == 0 && buyers[Slots[selectedSlot]].updatedAmount == 0) {
					deleteFile("Data/GrandExchange/Buyers/"+Slots[selectedSlot]+"");
					buyers[Slots[selectedSlot]].id = 0;
					buyers[Slots[selectedSlot]].itemId = 0;
					buyers[Slots[selectedSlot]].amount = 0;
					buyers[Slots[selectedSlot]].updatedAmount = 0;
					buyers[Slots[selectedSlot]].price = 0;
					buyers[Slots[selectedSlot]].percentage = 0;
					buyers[Slots[selectedSlot]].slot = 0;
					buyers[Slots[selectedSlot]].itemOne = 0;
					buyers[Slots[selectedSlot]].itemTwo = 0;
					buyers[Slots[selectedSlot]].itemOneAmount = 0;
					buyers[Slots[selectedSlot]].itemTwoAmount = 0;
					buyers[Slots[selectedSlot]].total = 0;
					buyers[Slots[selectedSlot]].totalGp = 0;
					buyers[Slots[selectedSlot]].aborted = false;
				    buyers[Slots[selectedSlot]].completed = false;
				    buyers[Slots[selectedSlot]].updated = false;
				    buyers[Slots[selectedSlot]].owner = "";
				    buyers[Slots[selectedSlot]] = null;
					Slots[selectedSlot] = 0;
					SlotType[selectedSlot] = 0;
					openGrandExchange(true);
					save = false;
				}
				if(save) {
				saveOffer(Slots[selectedSlot], "Buy");
				}
				recievedMessage = false;
				PlayerSave.saveGame(c);
				openCollect(selectedSlot, false);
			}
			if(i == 2) {
				if(buyers[Slots[selectedSlot]] == null) {
					return;
				}
				itemId = buyers[Slots[selectedSlot]].itemTwo;
					 if(ItemDefinition.forId(itemId).isNoteable())
						 itemId -= 1;
					 if(c.getItems().freeSlots() == 0) {
						 	c.sendMessage("You don't have enough inventory space.");
						 	return;
						 }
					 if(itemId >= 996 && itemId <= 1004) {
						 itemId = 995;
					 }
					 if(buyers[Slots[selectedSlot]].itemTwoAmount <= 0) {
						 return;
					 }
					 if(c.getItems().freeSlots() > 0 && ItemDefinition.forId(itemId).isStackable()) {
						 	c.getItems().addItem(itemId, buyers[Slots[selectedSlot]].itemTwoAmount);
						 	buyers[Slots[selectedSlot]].itemTwoAmount = 0;
						 }
					if(c.getItems().freeSlots() > 0 && !ItemDefinition.forId(itemId).isStackable()) {
						if(ItemDefinition.forId(itemId+1).isNoteable()) {
							 itemId += 1;
						} else {
							 return;
						}
							c.getItems().addItem(itemId, buyers[Slots[selectedSlot]].itemTwoAmount);
							buyers[Slots[selectedSlot]].itemTwoAmount = 0;
						}
					boolean save = true;
					if(buyers[Slots[selectedSlot]].itemOneAmount == 0 && buyers[Slots[selectedSlot]].itemTwoAmount == 0 && buyers[Slots[selectedSlot]].updatedAmount != 0) {
						buyers[Slots[selectedSlot]].updated = false;
					}
					if(buyers[Slots[selectedSlot]].itemOneAmount == 0 && buyers[Slots[selectedSlot]].itemTwoAmount == 0 && buyers[Slots[selectedSlot]].updatedAmount == 0) {
						deleteFile("Data/GrandExchange/Buyers/"+Slots[selectedSlot]+"");
						buyers[Slots[selectedSlot]].id = 0;
						buyers[Slots[selectedSlot]].itemId = 0;
						buyers[Slots[selectedSlot]].amount = 0;
						buyers[Slots[selectedSlot]].updatedAmount = 0;
						buyers[Slots[selectedSlot]].price = 0;
						buyers[Slots[selectedSlot]].percentage = 0;
						buyers[Slots[selectedSlot]].slot = 0;
						buyers[Slots[selectedSlot]].itemOne = 0;
						buyers[Slots[selectedSlot]].itemTwo = 0;
						buyers[Slots[selectedSlot]].itemOneAmount = 0;
						buyers[Slots[selectedSlot]].itemTwoAmount = 0;
						buyers[Slots[selectedSlot]].total = 0;
						buyers[Slots[selectedSlot]].totalGp = 0;
						buyers[Slots[selectedSlot]].aborted = false;
					    buyers[Slots[selectedSlot]].completed = false;
					    buyers[Slots[selectedSlot]].updated = false;
					    buyers[Slots[selectedSlot]].owner = "";
					    buyers[Slots[selectedSlot]] = null;
						Slots[selectedSlot] = 0;
						SlotType[selectedSlot] = 0;
						openGrandExchange(true);
						save = false;
					}
					if(save) {
					saveOffer(Slots[selectedSlot], "Buy");
					}
					recievedMessage = false;
					PlayerSave.saveGame(c);
					openCollect(selectedSlot, false);
				}
		}
	}
	
	/**
	 * Abort a offer
	 */
	public void abortOffer(int offer, boolean send) {
		if(sellers[Slots[offer]] != null && SlotType[offer] == 1) {
			if(sellers[Slots[offer]].completed == true) {
				c.sendMessage("Your offer is already completed!");
				return;
			}
			if(sellers[Slots[offer]].updatedAmount <= 0) {
				return;
			}
			if(send) {
				c.sendMessage("Abort request acknowledged. Please be aware that your offer may have already been");
				c.sendMessage("completed.");
			}
			if(stillSearching) {
				abortOffer(offer, false);
				return;
			}
			if(sellers[Slots[offer]].itemOneAmount == 0) {
				sellers[Slots[offer]].itemOne = sellers[Slots[offer]].itemId;
				sellers[Slots[offer]].itemOneAmount = sellers[Slots[offer]].updatedAmount;
				sellers[Slots[offer]].updatedAmount = 0;
				sellers[Slots[offer]].aborted = true;
			} else {
				sellers[Slots[offer]].itemTwo = sellers[Slots[offer]].itemId;
				sellers[Slots[offer]].itemTwoAmount = sellers[Slots[offer]].updatedAmount;
				sellers[Slots[offer]].updatedAmount = 0;
				sellers[Slots[offer]].aborted = true;
			}
			sellers[Slots[offer]].updated = true;
			saveOffer(Slots[offer], "Sell");
			openGrandExchange(false);
		} else if(buyers[Slots[offer]] != null && SlotType[offer] == 2) {
			if(buyers[Slots[offer]].completed == true) {
				c.sendMessage("Your offer is already completed!");
				return;
			}
			if(buyers[Slots[offer]].updatedAmount <= 0) {
				return;
			}
			if(send) {
				c.sendMessage("Abort request acknowledged. Please be aware that your offer may have already been");
				c.sendMessage("completed.");
			}
			if(stillSearching) {
				abortOffer(offer, false);
				return;
			}
			if(buyers[Slots[offer]].itemOneAmount == 0) {
				buyers[Slots[offer]].itemOneAmount = buyers[Slots[offer]].updatedAmount*buyers[Slots[offer]].price;
				buyers[Slots[offer]].itemOne = getMoneyStackId(buyers[Slots[offer]].itemOneAmount);
				buyers[Slots[offer]].updatedAmount = 0;
				buyers[Slots[offer]].aborted = true;
			} else {
				buyers[Slots[offer]].itemTwoAmount = buyers[Slots[offer]].updatedAmount*buyers[Slots[offer]].price;
				buyers[Slots[offer]].itemOne = getMoneyStackId(buyers[Slots[offer]].itemOneAmount);
				buyers[Slots[offer]].updatedAmount = 0;
				buyers[Slots[offer]].aborted = true;
			}
			buyers[Slots[offer]].updated = true;
			saveOffer(Slots[offer], "Buy");
			openGrandExchange(false);
		}
	}
	
    /**
     * Delete a file
     */
    public void deleteFile(String FileName) {
        String fileName = ""+FileName+".txt";
        File f = new File(fileName);
        if (!f.exists())
          throw new IllegalArgumentException("Delete: no such file or directory: " + fileName);
        if (!f.canWrite())
          throw new IllegalArgumentException("Delete: write protected: "+ fileName);
        if (f.isDirectory()) {
          String[] files = f.list();
          if (files.length > 0)
            throw new IllegalArgumentException("Delete: directory not empty: " + fileName);
        }
        boolean success = f.delete();
        if (!success)
          throw new IllegalArgumentException("Delete: deletion failed");
      }
    
}