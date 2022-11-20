package com.Howard;

import java.util.Arrays;
import java.util.List;

import com.Howard.Logging.LogConfiguration;

public class CGoL {
	/*Args: a single integer value N specifying the side length for the square game area 
	 * followed optionally by N integer values whose binary representation is used to specify the live cells in each row from top to bottom. 
	 */
	public static void main(String[] Args) throws Exception 
	{
		LogConfiguration.Configure();
		Game game;
		if(Args.length>0) {
			System.out.println(Arrays.asList(Args).toString());
			Integer size = Integer.valueOf(Args[0]);
			if(Args.length==1)
			{
				game = Game.gliderGame(size);
			}else {
				String[] arr = Arrays.copyOfRange(Args, 1, size+1);
				List<Integer> list = Arrays
					.asList(arr)
					.stream()
					.map((s)->s!=null?Integer.valueOf(s):0)
					.toList();
				game = new Game(size, list);
			}
		}else 
		{
			game = Game.randomGame(10);
		}
		
		
		
		int i=0;
		do
		{
			System.out.println(game);
			game.nextGeneration();
			Thread.sleep(50);
			i++;
		}while(i<100&&game.isLive());
		System.out.println(game);
	}
}
