using System;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading;
using MySql.Data.MySqlClient;

class Serverbloodg
{
    private static readonly string connectionString = "datasource=IP;port=PORT;username=root;password=;database=DATABASENAME";

    static void Main()
    {
        TcpListener server = null;

        try
        {
            IPAddress ipAddress = IPAddress.Parse("IP"); 
            int port = 8000;

            server = new TcpListener(ipAddress, port);
            server.Start();

            Console.WriteLine("Server started on " + ipAddress + ":" + port);

            while (true)
            {
                TcpClient client = server.AcceptTcpClient();
                Console.WriteLine("Client connected");

                Thread clientThread = new Thread(() => HandleClientCommunication(client));
                clientThread.Start();
            }
        }
        catch (Exception e)
        {
            Console.WriteLine("Error: " + e.Message);
        }
    }

    static void HandleClientCommunication(TcpClient client)
    {
        try
        {
            NetworkStream stream = client.GetStream();
            string message = ReadMessage(stream);
            Console.WriteLine($"Received: " + message);
            string responseMessage = ProcessParameters(message);
            WriteMessage(stream, responseMessage);
        }
        catch (Exception e)
        {
            Console.WriteLine("Error handling client: " + e.Message);
        }
        finally
        {
            client.Close();
            Console.WriteLine("Server closed");
        }
    }

    static string ReadMessage(NetworkStream stream)
    {
        byte[] data = new byte[256];
        int bytesRead = stream.Read(data, 0, data.Length);
        return Encoding.ASCII.GetString(data, 0, bytesRead);
    }

    static void WriteMessage(NetworkStream stream, string message)
    {
        byte[] data = Encoding.ASCII.GetBytes(message);
        stream.Write(data, 0, data.Length);
    }

    static string ProcessParameters(string message)
    {
        try
        {
            string[] words = message.Split('-');
            string action = words[0];
            string Username = words[1];
            string Password = words[2];
            string Email = words[3];
            string date = words[4];
            int b7am = int.Parse(words[5]);
            int b1pm = int.Parse(words[6]);
            int b7pm = int.Parse(words[7]);
            int bs = int.Parse(words[8]);

            if (action.Equals("INSERT"))
            {
                InsertUserData(Username, Password, Email, date, b7am, b1pm, b7pm, bs);
                return "Data inserted successfully!";
            }
            else if (action.Equals("RETRIEVE"))
            {
                string userData = RetrieveUserData(Username, Password, Email);
                return userData;
            }
            else
            {
                return "Invalid action specified!";
            }
        }
        catch (Exception ex)
        {
            return "Error processing message: " + ex.Message;
        }
    }

    static void InsertUserData(string Username, string Password, string Email, string date, int b7am, int b1pm, int b7pm, int bs)
    {
        try
        {
            using (MySqlConnection conn = new MySqlConnection(connectionString))
            {
                conn.Open();
                using (MySqlCommand cmd = new MySqlCommand("INSERT INTO `TABLENAME` (`Username`, `Password`, `Email`, `date`, `b7am`, `b1pm`, `b7pm`, `bs`) VALUES (@Username, @Password, @Email, @date, @b7am, @b1pm, @b7pm, @bs)", conn))
                {
                    cmd.Parameters.AddWithValue("@Username", Username);
                    cmd.Parameters.AddWithValue("@Password", Password);
                    cmd.Parameters.AddWithValue("@Email", Email);
                    cmd.Parameters.AddWithValue("@date", date);
                    cmd.Parameters.AddWithValue("@b7am", b7am);
                    cmd.Parameters.AddWithValue("@b1pm", b1pm);
                    cmd.Parameters.AddWithValue("@b7pm", b7pm);
                    cmd.Parameters.AddWithValue("@bs", bs);
                    cmd.ExecuteNonQuery();
                }
            }
        }
        catch (Exception ex)
        {
            Console.WriteLine("Error inserting data: " + ex.Message);
        }
    }

    static string RetrieveUserData(string Username, string Password, string Email)
    {
        try
        {
            StringBuilder data = new StringBuilder();

            using (MySqlConnection conn = new MySqlConnection(connectionString))
            {
                conn.Open();

                using (MySqlCommand cmd = new MySqlCommand("SELECT date, b7am, b1pm, b7pm, bs FROM TABLENAME WHERE Username = @Username AND Password = @Password", conn))
                {
                    cmd.Parameters.AddWithValue("@Username", Username);
                    cmd.Parameters.AddWithValue("@Password", Password);

                    using (MySqlDataReader reader = cmd.ExecuteReader())
                    {
                        while (reader.Read())
                        {
                            string rowData = $"{reader.GetString("date")}-{reader.GetInt32("b7am")}-{reader.GetInt32("b1pm")}-{reader.GetInt32("b7pm")}-{reader.GetInt32("bs")}";
                            data.AppendLine(rowData);
                        }
                    }
                }
                Console.WriteLine(data);

            }

            if (data.Length > 0)
            {
                return data.ToString();
            }
            else
            {
                return "No data found for the provided username and password";
            }
        }
        catch (Exception ex)
        {
            Console.WriteLine("Error retrieving user data: " + ex.Message);
            return "Error retrieving user data: " + ex.Message;
        }
    }

}
