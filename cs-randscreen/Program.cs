using System;

namespace ConsoleApplication
{
    public class Program
    {

        public static void Main(string[] args)
        {
        var rnd = new Random();
        var cols = Enum.GetValues(typeof(ConsoleColor));
        Func<ConsoleColor> randomCol =  () => (ConsoleColor)cols.GetValue(rnd.Next(cols.Length));

		var savedFg = Console.ForegroundColor;
		var savedBg = Console.ForegroundColor;

		while(true) {
			Console.ForegroundColor = randomCol();
			Console.BackgroundColor = randomCol();
			Console.SetCursorPosition(Console.WindowLeft + rnd.Next(Console.WindowWidth), 
                                                  Console.WindowTop + rnd.Next(Console.WindowHeight));
			Console.Write((char)(32 + rnd.Next(127-32)));
			System.Threading.Thread.Sleep(5);
			if(Console.KeyAvailable) break;
		}		

		Console.ForegroundColor = savedFg;
		Console.BackgroundColor = savedBg;
		Console.Clear();
        }
    }
}
