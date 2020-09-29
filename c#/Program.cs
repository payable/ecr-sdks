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
            Uri uri = new Uri("ws://192.168.10.237:45454");
            await conn.ConnectAsync(uri, CancellationToken.None).ConfigureAwait(false);
            Console.WriteLine("Connected...");
            while (true)
            {
                byte[] buffer = new byte[2048];
                await conn.ReceiveAsync(buffer, CancellationToken.None);
                var message = Encoding.UTF8.GetString(buffer, 0, buffer.Length);
                Console.WriteLine("from C#: " + message);
                await conn.SendAsync(Encoding.UTF8.GetBytes("from C#: " + message), WebSocketMessageType.Text, true, CancellationToken.None);
            }
        }
    }
}
