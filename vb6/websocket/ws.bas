Attribute VB_Name = "ws"
Option Explicit
Option Compare Text

Public Declare Sub CopyMemory Lib "kernel32" Alias "RtlMoveMemory" (Destination As Any, Source As Any, ByVal length As Long)
Public Enum OpcodeType
    opContin = 0
    opText = 1
    opBinary = 2
    
    opClose = 8
    opPing = 9
    opPong = 10
    
End Enum
Public Type DataFrame
    FIN             As Boolean
    RSV1            As Boolean
    RSV2            As Boolean
    RSV3            As Boolean
    Opcode          As OpcodeType
    MASK            As Boolean
    MaskingKey(3)   As Byte
    Payloadlen      As Long
    DataOffset      As Long
End Type

Private Const MagicKey = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11"
Private Const B64_CHAR_DICT = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/="

Public Function Handshake(requestHeader As String) As Byte()
    Dim clientKey   As String
    clientKey = getHeaderValue(requestHeader, "Sec-WebSocket-Key:")
    Dim AcceptKey   As String
    AcceptKey = getAcceptKey(clientKey)
    Dim response    As String
    response = "HTTP/1.1 101 Web Socket Protocol Handshake" & vbCrLf
    response = response & "Upgrade: WebSocket" & vbCrLf
    response = response & "Connection: Upgrade" & vbCrLf
    response = response & "Sec-WebSocket-Accept: " & AcceptKey & vbCrLf
    response = response & "WebSocket-Origin: " & getHeaderValue(requestHeader, "Sec-WebSocket-Origin:") & vbCrLf
    response = response & "WebSocket-Location: " & getHeaderValue(requestHeader, "Host:") & vbCrLf
    response = response & vbCrLf
    'Debug.Print response
    Handshake = StrConv(response, vbFromUnicode)
End Function

Private Function getHeaderValue(str As String, pname As String) As String
    Dim i           As Long, j As Long
    i = InStr(str, pname)
    If i > 0 Then
        j = InStr(i, str, vbCrLf)
        If j > 0 Then
            i = i + Len(pname)
            getHeaderValue = Trim(Mid(str, i, j - i))
        End If
    End If
End Function

Private Function getAcceptKey(key As String) As String
    Dim b()         As Byte
    b = SHA1(StrConv(key & "258EAFA5-E914-47DA-95CA-C5AB0DC85B11", vbFromUnicode))
    getAcceptKey = EnBase64(b)
End Function

Private Function EnBase64(str() As Byte) As String
    On Error GoTo over
    Dim buf()       As Byte, length As Long, mods As Long
    mods = (UBound(str) + 1) Mod 3
    length = UBound(str) + 1 - mods
    ReDim buf(length / 3 * 4 + IIf(mods <> 0, 4, 0) - 1)
    Dim i           As Long
    For i = 0 To length - 1 Step 3
        buf(i / 3 * 4) = (str(i) And &HFC) / &H4
        buf(i / 3 * 4 + 1) = (str(i) And &H3) * &H10 + (str(i + 1) And &HF0) / &H10
        buf(i / 3 * 4 + 2) = (str(i + 1) And &HF) * &H4 + (str(i + 2) And &HC0) / &H40
        buf(i / 3 * 4 + 3) = str(i + 2) And &H3F
    Next
    If mods = 1 Then
        buf(length / 3 * 4) = (str(length) And &HFC) / &H4
        buf(length / 3 * 4 + 1) = (str(length) And &H3) * &H10
        buf(length / 3 * 4 + 2) = 64
        buf(length / 3 * 4 + 3) = 64
    ElseIf mods = 2 Then
        buf(length / 3 * 4) = (str(length) And &HFC) / &H4
        buf(length / 3 * 4 + 1) = (str(length) And &H3) * &H10 + (str(length + 1) And &HF0) / &H10
        buf(length / 3 * 4 + 2) = (str(length + 1) And &HF) * &H4
        buf(length / 3 * 4 + 3) = 64
    End If
    For i = 0 To UBound(buf)
        EnBase64 = EnBase64 + Mid(B64_CHAR_DICT, buf(i) + 1, 1)
    Next
over:
End Function

Public Function AnalyzeHeader(byt() As Byte) As DataFrame
    Dim DF          As DataFrame
    DF.FIN = IIf((byt(0) And &H80) = &H80, True, False)
    DF.RSV1 = IIf((byt(0) And &H40) = &H40, True, False)
    DF.RSV2 = IIf((byt(0) And &H20) = &H20, True, False)
    DF.RSV3 = IIf((byt(0) And &H10) = &H10, True, False)
    DF.Opcode = byt(0) And &H7F
    DF.MASK = IIf((byt(1) And &H80) = &H80, True, False)
    Dim plen        As Byte
    plen = byt(1) And &H7F
    If plen < 126 Then
        DF.Payloadlen = plen
        If DF.MASK Then
            CopyMemory DF.MaskingKey(0), byt(2), 4
            DF.DataOffset = 6
        Else
            DF.DataOffset = 2
        End If
    ElseIf plen = 126 Then
        Dim l(3)    As Byte
        l(0) = byt(3)
        l(1) = byt(2)
        CopyMemory DF.Payloadlen, l(0), 4
        If DF.MASK Then
            CopyMemory DF.MaskingKey(0), byt(4), 4
            DF.DataOffset = 8
        Else
            DF.DataOffset = 4
        End If
    ElseIf plen = 127 Then
        DF.Payloadlen = -1
    End If
    AnalyzeHeader = DF
End Function

Public Sub PickDataV(byt() As Byte, dataType As DataFrame)
    Dim lenLimit    As Long
    lenLimit = dataType.DataOffset + dataType.Payloadlen - 1
    If dataType.MASK And lenLimit <= UBound(byt) Then
        Dim i       As Long, j As Long
        For i = dataType.DataOffset To lenLimit
            byt(i) = byt(i) Xor dataType.MaskingKey(j)
            j = j + 1
            If j = 4 Then j = 0
        Next i
    End If
End Sub

Public Function PickData(byt() As Byte, dataType As DataFrame) As Byte()
    Dim b()         As Byte
    PickDataV byt, dataType
    ReDim b(dataType.Payloadlen - 1)
    CopyMemory b(0), byt(dataType.DataOffset), dataType.Payloadlen
    PickData = b
End Function

Public Function PackString(str As String, Optional dwOpcode As OpcodeType = opText) As Byte()
    Dim b()         As Byte
    b = StrConv(str, vbFromUnicode)
    PackString = PackData(b, dwOpcode)
End Function

Public Function PackData(data() As Byte, Optional dwOpcode As OpcodeType = opText) As Byte()
    Dim length      As Long
    Dim byt()       As Byte
    length = UBound(data) + 1
    
    If length < 126 Then
        ReDim byt(length + 1)
        byt(1) = CByte(length)
        CopyMemory byt(2), data(0), length
    ElseIf length <= 65535 Then
        ReDim byt(length + 3)
        Dim l(1)    As Byte
        byt(1) = &H7E
        CopyMemory l(0), length, 2
        byt(2) = l(1)
        byt(3) = l(0)
        CopyMemory byt(4), data(0), length
    End If
    byt(0) = &H80 Or dwOpcode
    PackData = byt
End Function

Public Function PackMaskString(str As String) As Byte()
    Dim b()         As Byte
    b = StrConv(str, vbFromUnicode)
    PackMaskString = PackMaskData(b)
End Function
Public Function PackMaskData(data() As Byte) As Byte()
    Dim mKey(3)     As Byte
    mKey(0) = 108: mKey(1) = 188: mKey(2) = 98: mKey(3) = 208
    Dim i           As Long, j As Long
    For i = 0 To UBound(data)
        data(i) = data(i) Xor mKey(j)
        j = j + 1
        If j = 4 Then j = 0
    Next i
    
    Dim length      As Long
    Dim byt()       As Byte
    length = UBound(data) + 1
    If length < 126 Then
        ReDim byt(length + 5)
        byt(0) = &H81
        byt(1) = (CByte(length) Or &H80)
        CopyMemory byt(2), mKey(0), 4
        CopyMemory byt(6), data(0), length
    ElseIf length <= 65535 Then
        ReDim byt(length + 7)
        Dim l(1)    As Byte
        byt(0) = &H81
        byt(1) = &HFE
        CopyMemory l(0), length, 2
        byt(2) = l(1)
        byt(3) = l(0)
        CopyMemory byt(4), mKey(0), 4
        CopyMemory byt(8), data(0), length
     
    End If
    PackMaskData = byt
End Function


Public Function PingFrame() As Byte()
    Dim b(1)        As Byte
    b(0) = &H89
    b(1) = &H0
    PingFrame = b
    '0x89 0x05 0x48 0x65 0x6c 0x6c 0x6f
End Function
Public Function PongFrame() As Byte()
    Dim b(1)        As Byte
    b(0) = &H8A
    b(1) = &H0
    PongFrame = b
    '0x8A 0x05 0x48 0x65 0x6c 0x6c 0x6f
End Function
Public Function CloseFrame() As Byte()
    Dim b(1)        As Byte
    b(0) = &H88
    b(1) = &H0
    CloseFrame = b
End Function

Function wsEncode(payload() As Byte, Optional ByVal stype As String = "text", _
    Optional ByVal masked As Boolean = True) As Byte()
    Dim frame()     As Byte, frameLength As Long, payloadLength As Long, i As Long
    payloadLength = UBound(payload) + 1
    frameLength = 0
    ReDim frame(0)
    Select Case stype
        Case "text"
            '            // first byte indicates FIN, Text-Frame (10000001):
            frame(0) = 129
        Case "close"
            '            // first byte indicates FIN, Close Frame(10001000):
            frame(0) = 136
        Case "ping"
            '            // first byte indicates FIN, Ping frame (10001001):
            frame(0) = 137
        Case "pong"
            '            // first byte indicates FIN, Pong frame (10001010):
            frame(0) = 138
    End Select
    
    '        // set mask and payload length (using 1, 3 or 9 bytes)
    If payloadLength > 65535 Then
        '        payloadLengthBin = str_split(sprintf('%064b', payloadLength), 8);
        '        frame[1] = (masked === true) ? 255 : 127;
        '        for (i = 0; i < 8; i++) {
        '            frame[i + 2] = bindec(payloadLengthBin[i]);
        '        }
        '        // most significant bit MUST be 0 (close connection if frame too big)
        '        if (frame[2] > 127) {
        '            this->close(1004);
        '            throw new \RuntimeException('Invalid payload. Could not encode frame.');
        '        }
        frameLength = frameLength + 1
        ReDim Preserve frame(frameLength)
        frame(frameLength) = IIf(masked, 255, 127)
        Dim sBin    As String, sPart As String
        sBin = DecimalToBinary(payloadLength)
        Dim remainder As Long
        remainder = payloadLength Mod 256
        frameLength = frameLength + 8
        ReDim Preserve frame(frameLength)
        sPart = sBin
        For i = 7 To 0 Step -1
            frame(i + 2) = BinaryToDecimal(Right(sPart, 8))
            sPart = Left(sPart, Len(sPart) - 8)
            If sPart = "" Then
                Exit For
            End If
        Next
    ElseIf payloadLength > 125 Then
        '        payloadLengthBin = str_split(sprintf('%016b', payloadLength), 8);
        '        frame[1] = (masked === true) ? 254 : 126;
        '        frame[2] = bindec(payloadLengthBin[0]);
        '        frame[3] = bindec(payloadLengthBin[1]);
        frameLength = frameLength + 1
        ReDim Preserve frame(frameLength)
        frame(frameLength) = IIf(masked, 254, 126)
        sBin = DecimalToBinary(payloadLength, 2, IIf(payloadLength < 256, 16, 8))
        
        frameLength = frameLength + 1
        ReDim Preserve frame(frameLength)
        frame(frameLength) = BinaryToDecimal(Mid$(sBin, 1, 8))        ' payloadLength And Not 255
        frameLength = frameLength + 1
        ReDim Preserve frame(frameLength)
        frame(frameLength) = BinaryToDecimal(Mid$(sBin, 9, 8))        'payloadLength And 255
    Else
        frameLength = frameLength + 1
        ReDim Preserve frame(frameLength)
        frame(frameLength) = IIf(masked, payloadLength + 128, payloadLength)
    End If
    
    '        // convert frame-head to string:
    Dim eMask(3)    As Byte
    If masked = True Then
        '            // generate a random mask:
        Randomize
        For i = 0 To 3
            '                mask[i] = chr(rand(0, 255));
            eMask(i) = CByte(Rnd * 255)
        Next
        
        '            frame = array_merge(frame, mask);
        frameLength = frameLength + 4
        ReDim Preserve frame(frameLength)
        CopyMemory frame(frameLength - 3), eMask(0), 4
        
    End If
    
    '        // append payload to frame:
    frameLength = frameLength + payloadLength
    ReDim Preserve frame(frameLength)
    For i = 0 To payloadLength - 1
        '            frame .= (masked === true) ? payload[i] ^ mask[i % 4] : payload[i];
        If masked = True Then
            frame((frameLength - (payloadLength - 1)) + i) = payload(i) Xor eMask(i Mod 4) And 255
        Else
            frame((frameLength - (payloadLength - 1)) + i) = payload(i)
        End If
    Next
    
    wsEncode = frame
End Function

Public Function BinaryToDecimal(ByVal sBin As String, Optional ByVal BaseB As Currency = 2) As Currency
    Dim A           As Currency, C As Currency, D As Currency, E As Currency, F As Currency
    If sBin = "" Then
        Exit Function
    End If
    Do
        A = Val(Right$(sBin, 1))        'this is where the conversion happens
        sBin = Left$(sBin, Len(sBin) - 1)        'this is where the conversion happens
        If F > 49 Then        'overflow
        Exit Do
    End If
    C = BaseB ^ F        'conversion
    D = A * C        'conversion
    E = E + D        'conversion
    F = F + 1        'counter
Loop Until sBin = ""
BinaryToDecimal = E
End Function

Public Function DecimalToBinary(ByVal iDec As Currency, Optional ByVal BaseD As Currency = 2, _
       Optional ByVal bits As Long = 8) As String
    Dim bit         As Long, sBin As String, D As Currency
    DecimalToBinary = ""
    sBin = ""
    D = iDec
    bit = 1
    Do
        sBin = (D Mod BaseD) & sBin        'conversion
        D = D \ BaseD        'conversion
        If bit = 8 Or D = 0 Then
            bit = 0
            DecimalToBinary = Right$(String$(bits, "0") & sBin, bits) & DecimalToBinary
            sBin = ""
        End If
        bit = bit + 1
    Loop Until D = 0
    If sBin <> "" Then
        DecimalToBinary = Right$(String$(bits, "0") & sBin, bits) & DecimalToBinary
    End If
End Function

