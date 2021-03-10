using System;
using System.Net.WebSockets;
using System.Threading;
using System.Threading.Tasks;
using System.Text;

namespace MyApp
{
    class Program
    {
        static ClientWebSocket webSocket = null;
        static Thread messageThread = null;

        static void Main(string[] args)
        {
            StartCLI(null, null, null).Wait();
        }

        static async Task StartCLI(string ip, string posname, string reconnect)
        {
            try
            {
                // Console.Clear();
                Console.WriteLine("PAYable C# ECR Demonstration");
                Console.WriteLine("----------------------------");
                Console.Write("Enter IP Address: " + (ip != null ? ip + "\n" : ""));
                ip ??= Console.ReadLine();
                Console.Write("Enter POS Name: " + (posname != null ? posname + "\n" : ""));
                posname ??= Console.ReadLine();
                Console.Write("Do you need auto reconnect (y,n): " + (reconnect != null ? reconnect + "\n" : ""));
                reconnect ??= Console.ReadLine();
                Console.WriteLine("Please wait...");

                // 1. Connect WebSocket
                webSocket = new ClientWebSocket();
                webSocket.Options.KeepAliveInterval = TimeSpan.FromSeconds(5);
                Uri uri = new Uri("ws://" + ip + ":45454?token=4DqxynHGtHNckmCrRzvVxkwuSfr8faRmPrLIX0hmkqw=&pos=" + posname);
                await webSocket.ConnectAsync(uri, CancellationToken.None).ConfigureAwait(false);
                Console.WriteLine(ip + " is connected");

                while (webSocket.State == WebSocketState.Open)
                {
                    // 2. Listen for WebSocket responses
                    byte[] buffer = new byte[2048];
                    await webSocket.ReceiveAsync(buffer, CancellationToken.None);
                    string response = Encoding.UTF8.GetString(buffer, 0, buffer.Length);
                    Console.WriteLine("Response: " + response);

                    // 3. Sending payment request to terminal
                    messageThread = new Thread(async () =>
                     {
                         try
                         {
                             Console.WriteLine("----------------------------");
                             Console.Write("Enter amount: ");
                             string amount = Console.ReadLine();
                             Console.WriteLine("Please wait...");

                             if (webSocket.State == WebSocketState.Open)
                             {
                                 var request = "{\"amount\":" + amount + ",\"endpoint\":\"PAYMENT\",\"method\":\"CARD\"}";
                                 await webSocket.SendAsync(Encoding.UTF8.GetBytes(request), WebSocketMessageType.Text, true, CancellationToken.None);
                                 Console.WriteLine("Request sent to terminal");
                             }
                         }
                         catch (ThreadInterruptedException ex) { }
                     });

                    messageThread.Start();
                }
            }
            catch (Exception ex)
            {
                Console.ForegroundColor = ConsoleColor.Red;
                Console.WriteLine("\n" + ex.Message);
                Console.ResetColor();
            }
            finally
            {
                if (webSocket != null)
                    webSocket.Dispose();

                if (messageThread != null)
                    messageThread.Interrupt();

                if (reconnect.ToLower().Equals("y"))
                {
                    Console.WriteLine("\nRetrying...\n");
                    Thread.Sleep(3000);
                    StartCLI(ip, posname, reconnect).Wait();
                }
                else
                {
                    Console.Write("Press any key to continue...");
                    var s = Console.ReadLine();
                    StartCLI(null, null, null).Wait();
                }
            }
        }

        /**
         * 
         * Simple socket client connection
         *
        static async Task SimpleConnectAsync()
        {
            ClientWebSocket conn = new ClientWebSocket();
            Uri uri = new Uri("ws://192.168.1.16:45454?token=4DqxynHGtHNckmCrRzvVxkwuSfr8faRmPrLIX0hmkqw=&pos=COMPANY-1");
            await conn.ConnectAsync(uri, CancellationToken.None).ConfigureAwait(false);
            Console.WriteLine("Connected");
            while (true)
            {
                byte[] buffer = new byte[2048];
                await conn.ReceiveAsync(buffer, CancellationToken.None);
                var response = Encoding.UTF8.GetString(buffer, 0, buffer.Length);
                Console.WriteLine(response);

                var request = "{\"amount\":20,\"endpoint\":\"PAYMENT\",\"method\":\"CARD\"}";
                await conn.SendAsync(Encoding.UTF8.GetBytes(request), WebSocketMessageType.Text, true, CancellationToken.None);
            }
        }
        */
    }
}
