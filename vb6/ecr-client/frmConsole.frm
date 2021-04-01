VERSION 5.00
Object = "{248DD890-BB45-11CF-9ABC-0080C7E7B78D}#1.0#0"; "MSWINSCK.OCX"
Begin VB.Form formServer 
   BorderStyle     =   1  'Fixed Single
   Caption         =   "ECR Client"
   ClientHeight    =   4860
   ClientLeft      =   30
   ClientTop       =   300
   ClientWidth     =   6660
   LinkTopic       =   "frmConsole"
   MaxButton       =   0   'False
   MinButton       =   0   'False
   ScaleHeight     =   4860
   ScaleWidth      =   6660
   StartUpPosition =   2  'CenterScreen
   Begin VB.TextBox TextRequest 
      Height          =   375
      Left            =   1080
      TabIndex        =   4
      Text            =   "{""amount"":20,""endpoint"":""PAYMENT"",""method"":""CARD""}"
      Top             =   960
      Width           =   3015
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
      Left            =   4320
      TabIndex        =   1
      Top             =   960
      Width           =   979
   End
   Begin VB.CommandButton cmdCloseConsole 
      Caption         =   "Close"
      Height          =   374
      Left            =   5400
      TabIndex        =   0
      Top             =   960
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
      Height          =   2895
      Left            =   240
      MultiLine       =   -1  'True
      ScrollBars      =   3  'Both
      TabIndex        =   6
      Top             =   1680
      Width           =   6135
   End
   Begin VB.Label Label3 
      Caption         =   "Request:"
      Height          =   255
      Left            =   240
      TabIndex        =   5
      Top             =   1035
      Width           =   735
   End
   Begin VB.Label Label2 
      Alignment       =   2  'Center
      BackColor       =   &H80000007&
      Caption         =   "VB6 by PAYable | ECR Client"
      ForeColor       =   &H00FFFFFF&
      Height          =   255
      Left            =   2040
      TabIndex        =   3
      Top             =   240
      Width           =   2415
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
Private Sub btnSend_Click()
    sckClient.Close
    'sckClient.Protocol = IIf(True, 0, 2)
    sckClient.Connect "localhost", 45455
End Sub

Private Sub cmdCloseConsole_Click()
    Call sckServer.Close
    Call sckClient.Close
    Call Unload(formServer)
End Sub

Private Sub Form_Load()
    sckServer.LocalPort = 5050
    Call sckServer.Listen
    'lblVersion.Caption = "Version " & App.Major & "." & App.Minor & "." & App.Revision
End Sub


Private Sub sckClient_Connect()
    Dim sMessage As String
    sMessage = TextRequest.Text
    sckClient.SendData "POST / HTTP/1.1" & vbCrLf & _
        "Content-Type: application/json; charset=utf-8" & vbCrLf & _
        "Content-lenght: " & LenB(sMessage) & vbCrLf & vbCrLf & _
        sMessage & vbCrLf & vbCrLf
End Sub

Private Sub sckClient_DataArrival(ByVal bytesTotal As Long)
    Dim sBuffer As String
    sckClient.PeekData sBuffer
    sckClient.GetData sBuffer
    Debug.Print sBuffer
    TextLogs.Text = sBuffer & vbCrLf & TextLogs.Text
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
    TextLogs.Text = sRequest & vbCrLf & vbCrLf & TextLogs.Text
    sckServer.SendData (sContent)
End Sub

Private Sub sckServer_SendComplete()
    '// Reset the connection state after it as returned the requested file.
    With sckServer
        .Close
        .Listen
    End With
End Sub
