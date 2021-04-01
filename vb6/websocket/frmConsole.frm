VERSION 5.00
Object = "{248DD890-BB45-11CF-9ABC-0080C7E7B78D}#1.0#0"; "MSWINSCK.OCX"
Begin VB.Form formServer 
   BorderStyle     =   1  'Fixed Single
   Caption         =   "WebSocket"
   ClientHeight    =   6780
   ClientLeft      =   30
   ClientTop       =   300
   ClientWidth     =   6660
   LinkTopic       =   "frmConsole"
   MaxButton       =   0   'False
   MinButton       =   0   'False
   ScaleHeight     =   6780
   ScaleWidth      =   6660
   StartUpPosition =   2  'CenterScreen
   Begin VB.TextBox TextPosName 
      Height          =   375
      Left            =   1080
      TabIndex        =   11
      Text            =   "Cashier-VB"
      Top             =   1080
      Width           =   3375
   End
   Begin VB.TextBox TextAddress 
      Height          =   375
      Left            =   1080
      TabIndex        =   8
      Text            =   "192.168.1.16"
      Top             =   1680
      Width           =   3375
   End
   Begin VB.CommandButton btnConnect 
      Caption         =   "Connect"
      Height          =   374
      Left            =   4560
      TabIndex        =   7
      Top             =   1680
      Width           =   979
   End
   Begin VB.TextBox TextRequest 
      Height          =   855
      Left            =   1080
      TabIndex        =   4
      Text            =   "{""amount"":20,""endpoint"":""PAYMENT"",""method"":""CARD""}"
      Top             =   2280
      Width           =   5055
   End
   Begin MSWinsockLib.Winsock sckClient 
      Left            =   6000
      Top             =   3720
      _ExtentX        =   741
      _ExtentY        =   741
      _Version        =   393216
      LocalPort       =   6060
   End
   Begin VB.CommandButton btnSend 
      Caption         =   "Send"
      Height          =   374
      Left            =   2640
      TabIndex        =   1
      Top             =   3240
      Width           =   1455
   End
   Begin VB.CommandButton cmdCloseConsole 
      Caption         =   "Close"
      Height          =   374
      Left            =   5640
      TabIndex        =   0
      Top             =   1680
      Width           =   979
   End
   Begin MSWinsockLib.Winsock sckServer 
      Left            =   6000
      Top             =   4200
      _ExtentX        =   741
      _ExtentY        =   741
      _Version        =   393216
      LocalPort       =   5050
   End
   Begin VB.TextBox TextLogs 
      Height          =   2655
      Left            =   240
      MultiLine       =   -1  'True
      ScrollBars      =   3  'Both
      TabIndex        =   6
      Top             =   3840
      Width           =   6135
   End
   Begin VB.Label Label6 
      Caption         =   "POS Name:"
      Height          =   375
      Left            =   120
      TabIndex        =   12
      Top             =   1200
      Width           =   1095
   End
   Begin VB.Label Label5 
      Caption         =   "Response:"
      Height          =   375
      Left            =   240
      TabIndex        =   10
      Top             =   3480
      Width           =   735
   End
   Begin VB.Label Label4 
      Caption         =   "Address:"
      Height          =   375
      Left            =   120
      TabIndex        =   9
      Top             =   1680
      Width           =   975
   End
   Begin VB.Label Label3 
      Caption         =   "Request:"
      Height          =   255
      Left            =   240
      TabIndex        =   5
      Top             =   2520
      Width           =   735
   End
   Begin VB.Label Label2 
      Alignment       =   2  'Center
      BackColor       =   &H80000007&
      Caption         =   "VB6 Demo by PAYable | WebSocket"
      ForeColor       =   &H00FFFFFF&
      Height          =   255
      Left            =   1920
      TabIndex        =   3
      Top             =   240
      Width           =   2895
   End
   Begin VB.Label Label1 
      Alignment       =   2  'Center
      BackColor       =   &H80000007&
      Height          =   735
      Left            =   0
      TabIndex        =   2
      Top             =   0
      Width           =   6715
   End
End
Attribute VB_Name = "formServer"
Attribute VB_GlobalNameSpace = False
Attribute VB_Creatable = False
Attribute VB_PredeclaredId = True
Attribute VB_Exposed = False
Private Sub btnConnect_Click()
    sckClient.Close
    sckClient.LocalPort = 0
    sckClient.Connect TextAddress.Text, 45454
End Sub

Private Sub btnSend_Click()
    sckClient.SendData PackMaskString(TextRequest.Text)
End Sub

Private Sub cmdCloseConsole_Click()
    sckServer.Close
    sckClient.Close
    Unload formServer
End Sub

Private Sub Form_Load()
    sckServer.LocalPort = 5050
    Call sckServer.Listen
    'lblVersion.Caption = "Version " & App.Major & "." & App.Minor & "." & App.Revision
End Sub

Private Sub sckClient_Close()
    Debug.Print "sckClient_Close"
End Sub

Private Sub sckClient_Connect()
    Debug.Print "sckClient_Connect"
    Dim Packet As String
    Packet = "GET /?token=4DqxynHGtHNckmCrRzvVxkwuSfr8faRmPrLIX0hmkqw=&pos=" & TextPosName.Text & " HTTP/1.1" & vbCrLf & _
        "Upgrade: websocket" & vbCrLf & _
        "Connection: Upgrade" & vbCrLf & _
        "Sec-WebSocket-Key: dGhlIHNhbXBsZSBub25jZQ==" & vbCrLf & _
        "Connection: close" & vbCrLf & _
        "Sec-WebSocket-Version: 13" & vbCrLf & vbCrLf
    sckClient.SendData Packet
End Sub

Private Sub sckClient_DataArrival(ByVal bytesTotal As Long)

    Dim sBuffer As String
    Dim byt() As Byte
    Dim DF As DataFrame
    sckClient.GetData sBuffer
    Debug.Print sBuffer
    byt = StrConv(sBuffer, vbFromUnicode)
    
    DF = AnalyzeHeader(byt)
    
    Select Case DF.Opcode
        Case OpcodeType.opContin:   Debug.Print "opContin"
        Case OpcodeType.opText
            Debug.Print "opText"
            TextLogs.Text = Mid(sBuffer, InStr(1, sBuffer, "{", vbTextCompare)) & vbCrLf & TextLogs.Text
        Case OpcodeType.opBinary:   Debug.Print "opBinary"
        Case OpcodeType.opClose
            Debug.Print "opClose"
            'GoTo errFrame
        Case OpcodeType.opPing
            Debug.Print "opPing"
            'sckClient.SendData PackMaskData(StrConv(" ", vbFromUnicode))
            sckClient.SendData wsEncode(byt, "pong", True)
            'sckClient.SendData PackMaskString("0")
            Exit Sub
        Case OpcodeType.opPong
            Debug.Print "opPong"
            sckClient.SendData PongFrame()
            Exit Sub
        Case Else
            Debug.Print "Else"
    End Select
    
    'Dim sBuffer As String
    'sckClient.PeekData sBuffer
    'sckClient.GetData sBuffer
    'Debug.Print sBuffer
    'TextLogs.Text = sBuffer & vbCrLf & TextLogs.Text
    'sckClient.SendData PackMaskString("{}")
End Sub

Private Sub sckClient_Error(ByVal Number As Integer, Description As String, ByVal Scode As Long, ByVal Source As String, ByVal HelpFile As String, ByVal HelpContext As Long, CancelDisplay As Boolean)
    Debug.Print Description
End Sub

Private Sub sckServer_ConnectionRequest(ByVal requestID As Long)
    '// Accept the incomming connection from a browser.
    With sckServer
        .Close
        .Accept (requestID)
    End With
End Sub

Private Sub sckServer_DataArrival(ByVal bytesTotal As Long)
    '// Retreive the incomming data and return the requested file.
    Dim sRequest As String
    Dim sBody As String
    Dim sContent As String
    Dim rBody As String
    sckServer.GetData sRequest, vbString
    If InStr(1, sRequest, "{", vbTextCompare) > 0 Then
         rBody = Mid(sRequest, InStr(1, sRequest, "{", vbTextCompare))
    Else
        rBody = "{""version"":""1.0""}"
    End If
    sBody = rBody
    sContent = "HTTP/1.1 200 OK" & vbCrLf & _
        "Content-Type: text/plain; charset=utf-8" & vbCrLf & _
        "Content-Length: " & Len(sBody) & vbCrLf & _
        "Connection: close" & vbCrLf & vbCrLf & _
        sBody & vbCrLf & vbCrLf
    
    Debug.Print rBody & vbCrLf
    TextLogs.Text = sRequest & vbCrLf & TextLogs.Text
    sckServer.SendData (sContent)
End Sub

Private Sub sckServer_SendComplete()
    '// Reset the connection state after it as returned the requested file.
    With sckServer
        .Close
        .Listen
    End With
End Sub
