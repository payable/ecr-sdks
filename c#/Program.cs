using System;
using System.Net.WebSockets;
using System.Threading;
using System.Threading.Tasks;
using System.Text;

namespace MyApp
{
    class Program
    {
        static void Main(string[] args)
        {
            ConnectAsync().Wait();
        }

        static async Task ConnectAsync()
        {
            ClientWebSocket conn = new ClientWebSocket();
            Uri uri = new Uri("ws://192.168.1.16:45454?token=4DqxynHGtHNckmCrRzvVxkwuSfr8faRmPrLIX0hmkqw=&pos=COMPANY-1");
            await conn.ConnectAsync(uri, CancellationToken.None).ConfigureAwait(false);
            Console.WriteLine("Connected...");
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
    }
}
