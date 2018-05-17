import java.io.IOException;
import java.math.BigDecimal;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.gamereward.grd.GrdAccountInfo;
import io.gamereward.grd.GrdCustomResult;
import io.gamereward.grd.GrdLeaderBoard;
import io.gamereward.grd.GrdManager;
import io.gamereward.grd.GrdNet;
import io.gamereward.grd.GrdResult;
import io.gamereward.grd.GrdResultBase;
import io.gamereward.grd.GrdSessionData;
import io.gamereward.grd.GrdTransaction;
public class Test {
	static Scanner scanner = new Scanner(System.in);
	static final String scoreType="GAME1_SCORE_TYPE";
	static String username;
	static String userSelect;
	static void clearScreen() {
		System.out.flush();
	}

	static void pauseScreen() {
		System.out.print("Press enter to continue...");
		try {
			System.in.read();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	static void printError(GrdResultBase error){
		System.out.println("ERROR:" + error.error + ",MESSAGE:" + error.message );
		pauseScreen();
	}
	static String formatField(String st,int len){
		final String sfixed = "                                                                                                                       ";
		if (st.length() < len){
			st = sfixed.substring(sfixed.length() - len+st.length()) + st;
		}
		return st;
	}
	/*LEADER BOARD*/

	static void game_leaderboard(String title,String scoreType){
		System.out.println( title);
		System.out.println("*********************************************************");
		GrdResult<GrdLeaderBoard[]>result = GrdManager.getLeaderBoard(username, scoreType, 0, 20);
		if (result.error==0){
			System.out.println("+-RANK---+----NAME------------------------------------+---SCORE--+");
			for (GrdLeaderBoard item:result.data){
				System.out.println("|" + formatField(item.rank+"", 8) +"|" + formatField(item.username, 44) + "|" + formatField(item.score+"", 10) + "|");
				System.out.println("+--------+--------------------------------------------+----------+");
			}
			pauseScreen();
		}
		else{
			printError(result);
		}
	}
	/*ACCOUNT API TEST*/
	static void transfer(){
		clearScreen();
		String to;
		String svalue;
		BigDecimal value=BigDecimal.ZERO;
		System.out.println("TRANSFER MONEY");
		System.out.println( "-----------------------------------");
		System.out.print( "TO ADDRESS:");
		to= scanner.nextLine();;
		if (to.length() == 0){
			System.out.println( "INVALID ADDRESS!");
			pauseScreen();
			return;
		}
		do{
			System.out.print( "AMOUNT:");
			svalue=scanner.nextLine();
			try{
				value = new BigDecimal(svalue);
			}
			catch (Exception e){
			}
			if (value.compareTo(BigDecimal.ZERO)<=0){
				System.out.println( "The money need to be greater than 0.");
			}
			else{
				break;
			}
		} while (true);
		System.out.println( "***************WARNING*************");
		System.out.println( "IT IS REAL MONEY!");
		System.out.println( "This action will transfer money from this account to " + to + "");
		System.out.println( "***********************************");
		System.out.print( "Please confirm this action (YES to confirm,other to cancel):");
		svalue=scanner.nextLine();
		if (svalue.equals("YES")){
			
			GrdResultBase result=GrdManager.transfer(username, to, value);
			if (result.error!=0){
				printError(result);
				return;
			}
			else{
				System.out.println( "TRANSFER SUCCESSFULLY!");
				pauseScreen();
				return;
			}
		}
	}
	static void chargeMoney(){
		System.out.println( "CHARGE MONEY");
		String svalue;
		BigDecimal value=BigDecimal.ZERO;
		System.out.println( "-----------------------------------");
		do{
			System.out.print( "AMOUNT TO CHARGE:");
			svalue=scanner.nextLine();
			try{
				value = new BigDecimal(svalue);
			}
			catch (Exception e){
			}
			if (value.compareTo(BigDecimal.ZERO)<=0){
				System.out.println( "The money need to be greater than 0.");
			}
			else{
				break;
			}
		} while (true);
		GrdResultBase result=GrdManager.chargeMoney(username, value);
		if (result.error!=0){
			printError(result);
		}
		else{
			System.out.println( "CHARGE SUCCESSFULLY!");
			pauseScreen();
		}
	}

	static void payMoney(){
		System.out.println( "PAY MONEY");
		String svalue;
		BigDecimal value=BigDecimal.ZERO;
		System.out.println( "-----------------------------------");
		do{
			System.out.print( "AMOUNT TO PAY TO USER:");
			svalue=scanner.nextLine();
			try{
				value = new BigDecimal(svalue);
			}
			catch (Exception e){
			}
			if (value.compareTo(BigDecimal.ZERO)<=0){
				System.out.println( "The money need to be greater than 0.");
			}
			else{
				break;
			}
		} while (true);		
		//Pay money value need to be < 0
		value = value.negate();
		GrdResultBase result= GrdManager.chargeMoney(username, value);
		if (result.error!=0){
			printError(result);
		}
		else{
			System.out.println("PAY SUCCESSFULLY!");
			pauseScreen();
		}
	}
	static void listTransactions(){
		int pageSize = 10;
		int pageIndex = 0;
		GrdResult<Integer> countResult = GrdManager.getTransactionCount(username);
		if (countResult.error!=0){
			printError(countResult);
		}
		else{
			do{
				clearScreen();
				System.out.println( "TRANSACTIONS");
				System.out.println( "*********************************************************");
				GrdResult<GrdTransaction[]>trans = GrdManager.getTransactions(username, pageIndex*pageSize, pageSize);
				if (trans.error==0){
					for (GrdTransaction tran:trans.data){
						System.out.println( "------------------------------------------------------------");
						System.out.println( "tx:" + tran.tx + "");
						System.out.println( "time:" + tran.getTime().toString() + "");
						System.out.println( "from:" + tran.from + "");
						System.out.println( "to:" + tran.to + "");
						System.out.println( "amount:" + tran.amount.toString() + "");
						System.out.println( "type:" + (tran.transtype == GrdManager.INTERNAL_TRANSTYPE ? "Internal" : "External") + "");
						System.out.println( "status:" + (tran.status == GrdManager.SUCCESS_TRANSSTATUS ? "Success" : (tran.status == GrdManager.PENDING_TRANSSTATUS ? "Pending" : "Error")) + "");
						System.out.println( "------------------------------------------------------------");
					}

					System.out.println( "*********************************************************");
					int pageCount = (int)Math.ceil((double)countResult.data / pageSize);
					System.out.println( "Page:" + (pageIndex + 1) + "/" + pageCount + "| Next:1-Prev:2-Exit:10");
					System.out.print( "YOUR CHOISE:");
					userSelect=scanner.nextLine();
					if (userSelect.equals("2")){
						if (pageIndex > 0){
							pageIndex--;
						}
					}
					else if (userSelect.equals("1")){
						if (pageIndex < pageCount - 1){
							pageIndex++;
						}
					}
					else if (userSelect.equals("10")){
						return;
					}
				}
				else{
					printError(trans);
				}

			} while (true);
		}
	}
	static void accountInfo(){
		while (true)
		{
			clearScreen();
			GrdResult<GrdAccountInfo> result= GrdManager.accountbalance(username);
			System.out.println( "ACCOUNT INFORMATION");
			System.out.println( "-----------------------------------");
			if (result.error!=0){
				printError(result);
			}
			else{
				System.out.println( "Username:" + username + "");
				System.out.println( "Wallet address:" + result.data.address + "");
				System.out.println( "Balance:" +  result.data.balance.toString() + "");
			}
			System.out.println( "-----------------------------------");
			System.out.println( "TO DO");
			System.out.println( "1.Transfer money to other address (use for user).");
			System.out.println( "2.Charge from this account (use for game action).");
			System.out.println( "3.Pay money to this account(use for game action).");
			System.out.println( "4.List transactions");
			System.out.println( "5.Refresh.");
			System.out.println( "10.Go back...");
			System.out.println( "-----------------------------------");
			System.out.print( "YOUR CHOISE:");
			 userSelect=scanner.nextLine();
			if (userSelect.equals("1"))
			{
				transfer();
			}
			else if (userSelect.equals("2")){
				chargeMoney();
			}
			else if (userSelect.equals("3")){
				payMoney();
			}
			else if (userSelect.equals("4")){
				listTransactions();
			}
			else if (userSelect.equals("10")){
				return;
			}
		}
	}
	/*END ACCOUNT TEST*/
	/*SCRIPT SERVER API*/

	static void random09_history(){
		clearScreen();
		System.out.println( "RANDOM 1-9 HISTORY");
		System.out.println( "*********************************************************");
		GrdResult<GrdSessionData[]> result = GrdManager.getUserSessionData(username, "GAME-9","rand", 0, 20);
		if (result.error==0){
			System.out.println( "+-TIME-------------------------+---SELECT--+----RESULT----+-----MONEY-----+");
			for (GrdSessionData dt:result.data){
				if (dt.values.containsKey("rand")){
					String value = dt.values.get("rand");
					String yourNumber="";
					String randNumber="";
					String money;
					int ipos = value.indexOf(",");
					if (ipos > 0){
						yourNumber = value.substring(0, ipos);
						value = value.substring(ipos + 1);
					}
					ipos = value.indexOf(",");
					if (ipos > 0){
						randNumber = value.substring(0, ipos);
						value = value.substring(ipos + 1);
					}
					money = value;
					System.out.println( "|" + formatField(dt.getTime().toString(), 30) + "|" + formatField(yourNumber, 11) + "|" + formatField(randNumber, 14) + "|" + formatField(money, 15) + "|");
					System.out.println( "+------------------------------+-----------+--------------+---------------+");
				}
			}
			pauseScreen();
		}
		else{
			printError(result);
		}
	}
	static void random09Game(){
		String svalue;
		int number;
		BigDecimal value;
		System.out.println( "RANDOM 1-9 GAME");
		while (true)
		{
			do{
				clearScreen();		
				System.out.println( "-----------------------------------");
				System.out.println( "1-9:YOUR LUCKY NUMBER.");
				System.out.println( "10. LEADER BOARD.");
				System.out.println( "11. HISTORY.");
				System.out.println( "100. EXIT.");
				System.out.println( "-----------------------------------");
				System.out.print( "SELECT:");
				svalue=scanner.nextLine();
				if(svalue.length()>0) {
					number = Integer.parseInt(svalue);
					if ( number>=1&& number<=9)
					{
						break;
					}
					if (number==10){
						clearScreen();
						game_leaderboard("RANDOM 1-9 GAME LEADER BOARD","random9_score");
					}
					if (number==11){
						random09_history();
					}
					if (number==100){
						return;
					}
				}
			} while (true);
			do{
				System.out.print( "BET:");
				svalue=scanner.nextLine();
				try{
					value =new BigDecimal(svalue);
					if (value.compareTo(BigDecimal.ZERO)>0){
						break;
					}
				}
				catch (Exception e){
				}
				System.out.println("Bet must be greater than 0!");
			} while (true);
			GrdCustomResult result = GrdManager.callServerScript(username, "testscript", "random9", new Object[] {number,value});
			if (result.error!=0){
				printError(result);
			}
			else{
				//Server response an array
				JSONArray jsonArray=	(JSONArray)result.data;
				try {
					if (jsonArray.get(0).toString().equals("0")){
						System.out.println( "SELECT:" + jsonArray.getInt(2) + ",RESULT:" + jsonArray.getInt(1) + "");
						System.out.println( (jsonArray.getDouble(3) > 0 ? "WIN:" : "LOSE:") + jsonArray.getDouble(3) + "");
					}
					else{
						System.out.println(jsonArray.getString(1));//Message in game
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				pauseScreen();
			}
		}
	}

	static void lowhighgame_history(){
		clearScreen();
		System.out.println( "LOW-HIGH GAME HISTORY");
		System.out.println( "*********************************************************");
		GrdResult<GrdSessionData[]>result = GrdManager.getUserSessionData(username, "LOWHIGHGAME", "result", 0, 20);
		if (result.error==0){
			System.out.println( "+-TIME-------------------------+-----CARD-----+---SELECT--+----RESULT----+-----MONEY-----+");
			for (GrdSessionData dt :result.data){
				//Contains the rand key
				if (dt.values.containsKey("result")){
					String value=dt.values.get("result");
					//Read the array result
					JSONArray jValue;
					try {
						jValue = new JSONArray(value);
						boolean islow = jValue.getInt(0) == 1;
						int yourNumber = jValue.getInt(1);
						int randNumber = jValue.getInt(2);
						double money =jValue.getDouble(3);
						//
						System.out.println( "|" + formatField(dt.getTime().toString(), 30) + "|" + formatField(yourNumber+"", 14) + "|" + formatField((islow?"LOW":"HIGH"), 11) + "|" + formatField(randNumber+"", 14) + "|" + formatField(money+"", 15) + "|");
						System.out.println( "+------------------------------+--------------+-----------+--------------+---------------+");
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				
				}
			}
			pauseScreen();
		}
		else{
			printError(result);
		}
	}
	static void highlowgame(){
		String svalue;
		double number;
		BigDecimal value;
		boolean islow = false;
		int LOW = 3;
		int HIGH = 13;
		while (true)
		{
			clearScreen();
			System.out.println( "HIGH-LOW GAME");
			System.out.println( "-----------------------------------");
			number =Math.round(Math.random()*(HIGH - LOW)) + LOW;
			System.out.println( "1. LOW: 2 To " + number + "(Bet Rate:" + ((14 - number) / (number - 2)) + "/1)");
			System.out.println( "2. HIGH: " + number + " To 14(Bet Rate:" + ((number - 2) / (14 - number)) + "/1)");
			System.out.println( "3. RANDOM NEXT");
			System.out.println( "4. LEADER BOARD");
			System.out.println( "5. HISTORY");
			System.out.println( "10. EXIT...");
			System.out.print( "SELECT:");
			svalue=scanner.nextLine();
			if ((svalue.equals("1")) || (svalue.equals("2")))
			{
				islow = svalue.equals("1");
			}
			else if (svalue.equals("4")){
				clearScreen();
				game_leaderboard("LOW HIGH GAME LEADER BOARD", "lowhighgame_score");
				continue;
			}
			else if (svalue.equals("5")){
				clearScreen();
				lowhighgame_history();
				continue;
			}
			else if (svalue.equals("10")){
				return;
			}
			else{
				continue;
			}
			do{
				System.out.print( "BET:");
				svalue=scanner.nextLine();
				try{
					value =new BigDecimal(svalue);
					if (value.compareTo(BigDecimal.ZERO)>0){
						break;
					}
				}
				catch (Exception e){
				}
				System.out.println( "Bet must be greater than 0!");
			} while (true);
			GrdCustomResult result = GrdManager.callServerScript(username, "testscript", "lowhighgame", new Object[] {islow?"1":"0",number,value});
			if (result.error!=0){
				printError(result);
			}
			else{
				//Server response an array
				JSONArray jsonArray=(JSONArray)result.data;
				try {
					if (jsonArray.getInt(0)==0){
						JSONObject jobj= (JSONObject)jsonArray.get(1);
						int symbol=jobj.getInt("symbol");
						double money = jsonArray.getDouble(2);
						System.out.println( "NUMBER:" + number + ",SELECT:" + (islow ? "LOW" : "HIGH") + ",RESULT:" + symbol + "");
						System.out.println( (money > 0 ? "WIN:" : "LOSE:") + money + "");
					}
					else{
						System.out.println(jsonArray.getString(1));//Message in game
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				pauseScreen();
			}
		}
	}
	static void scriptServerMenu(){
		do{
			clearScreen();
			System.out.println( "SCRIPT SERVER");
			System.out.println( "-----------------------------------");
			System.out.println( "1. RANDOM 1-9 GAME.");
			System.out.println( "2. HIGH LOW GAME.");
			System.out.println( "10. Exit.");
			System.out.println( "-----------------------------------");
			System.out.print( "SELECT:");
			userSelect=scanner.nextLine();
			if (userSelect.equals("1"))
			{
				random09Game();
			}
			else if (userSelect.equals("2"))
			{
				highlowgame();
			}
			else if (userSelect.equals("10"))
			{
				break;
			}
		} while (true);
	}
	static void setScore(){
		String value;
		double score;
		System.out.println( "-----------------------------------");
		System.out.print( "SET USER SCORE:");
		value=scanner.nextLine();
		score = Double.parseDouble(value);
		GrdResultBase result= GrdManager.saveUserScore(username, scoreType, score);
		if (result.error!=0){
			printError(result);
		}
		else{
			System.out.println( "SAVE SCORE SUCCESSFULLY!");
			pauseScreen();
		}
	}
	static void increaseScore(){
		String value;
		double score;
		System.out.println( "-----------------------------------");
		System.out.print( "INCREASE USER SCORE:");
		value=scanner.nextLine();
		score = Double.parseDouble(value);
		GrdResultBase result= GrdManager.increaseUserScore(username, scoreType, score);
		if (result.error!=0){
			printError(result);
		}
		else{
			System.out.println( "INCREASE SCORE SUCCESSFULLY!");
			pauseScreen();
		}
	}

	static void ScoreApiTest(){
		while (true)
		{
			GrdResult<GrdLeaderBoard>result= GrdManager.getUserScoreRank(username, scoreType);
			if(result.error==0) {
				clearScreen();
				System.out.println( "SCORE API TEST");
				System.out.println( "Score type test:" + scoreType + "");
				System.out.println( "User score:" + result.data.score + "");
				System.out.println( "User rank:" + result.data.rank + "");
				System.out.println( "-----------------------------------");
				System.out.println( "1. Set score");
				System.out.println( "2. Increase score");
				System.out.println( "3. Leader board");
				System.out.println( "10.Exit.");
				System.out.println( "-----------------------------------");
				System.out.print( "YOUR CHOISE:");
				 userSelect=scanner.nextLine();
				if (userSelect.equals("1"))
				{
					setScore();
				}
				else if (userSelect.equals("2"))
				{
					increaseScore();
				}
				else if (userSelect.equals("3"))
				{
					game_leaderboard("GAME LEADER BOARD SCORETYPE:"+scoreType, scoreType);
				}
				else if (userSelect.equals("10"))
				{
					return;
				}
			}
			else {
				printError(result);
				return;
			}
			
		}
	}
	static void testMenu(){
		while (true)
		{
			clearScreen();
			System.out.println("SELECT MENU");
			System.out.println( "-----------------------------------");
			System.out.println( "1.Account API.");
			System.out.println( "2.Scores API.");
			System.out.println( "3.Script Server-OAPI.");
			System.out.println( "10.Exit.");
			System.out.println( "-----------------------------------");
			System.out.print( "YOUR CHOISE:");
			userSelect=scanner.nextLine();
			if (userSelect.equals("1"))
			{
				accountInfo();
			}
			else if (userSelect.equals("2"))
			{
				ScoreApiTest();
			}
			else if (userSelect.equals("3"))
			{
				scriptServerMenu();
			}
			else if (userSelect.equals("10"))
			{
				return;
			}
		}
	}
	
	public static void main(String[]args) {
		final String appId = "6e672e888487bd8346b946a715c74890077dc332";
		final String secret = "acc3e0404646c57502b480dc052c4fe15878a7ab84fb43402106c575658472faf7e9050c92a851b0016442ab604b0488aab3e67537fcfda3650ad6cfd43f7974";
		GrdManager.init(appId, secret,GrdNet.TestNet);
		clearScreen();
		System.out.print( "USER NAME:");
		username = scanner.nextLine();
		if (username.length() == 0){
			return;
		}
		GrdManager.accountbalance(username);//create user if is new user
		testMenu();
	}
}
