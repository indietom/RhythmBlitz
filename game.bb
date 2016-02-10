Global winW = 320, winH = 240

AppTitle "Rhythm Blitz"

Graphics winW, winH, 8, 2

SeedRnd MilliSecs()

Global frametimer=CreateTimer(60)
Global starttime=MilliSecs(),elapsedtime,fpscounter,curfps

Global spritesheet = LoadImage("spritesheet.bmp")
MaskImage(spritesheet, 255, 0, 255)

Global downSnd 
Global upSnd 
Global leftSnd 
Global rightSnd 

downSnd = LoadSound("down.wav")
upSnd = LoadSound("up.wav")
leftSnd = LoadSound("left.wav")
rightSnd = LoadSound("right.wav")

Function collision(x, y, w, h, x2, y2, w2, h2)
	If y >= y2 + h2 Then Return False 
	If x >= x2 + w2 Then Return False 
	If y + h <= y2 Then Return False
	If x + w <= x2 Then Return False   
	 Return True 
End Function 

Function lerp#(x#, y#, t#)
	Return t# * y# + (1-t#) * x#
End Function

Global highscore = 0

Local ifile = OpenFile("highscore.txt")
highscore% = Int(ReadLine(ifile))
CloseFile(ifile)

Function updateHighScore() 
	If score > highscore Then
		highscore = score
	End If
End Function

Function getFileLength(path$)
	Local file = OpenFile(path)
	Local count = 0
	
	While Not Eof(file)
		ReadLine(file)
		count = count + 1
	Wend
	
	CloseFile(file)
End Function

Function clamp#(v#, min#, max#)
	If v < min Then Return min
	If v > max Then Return max
	Return v
End Function 

Function removeChar$(org$, offset)
	Local length = Len(org$)
	Local tmp$ = ""
	
	For i = 1 To length
		If i <> offset Then 
			tmp = tmp + Mid(org, i, 1)
		End If
	Next 
	
	Return tmp
End Function

Function randomList$(length)
	Local tmp$ = ""
	
	For i = 0 To length
		tmp = tmp + randomLetter(Rand(0, 3))
	Next
	
	Return tmp
End Function

Function randomLetter$(n)
	Select n
		Case 0
			Return "L"
		Case 1
			Return "R"
		Case 2
			Return "D"
		Case 3
			Return "U"
	End Select 
End Function

Type event
	Field x#
	Field y#
	
	Field orgX#
	Field orgY#
	
	Field list$
	Field startLength
	
	Field worth
	
	Field destroy
End Type

Const SHAKE_MANGNITUDE = 16

Function addEvent(x2#, y2#, list2$) 
	e.event = New event
	e\x = x2
	e\y = y2
	
	e\orgX = x2
	e\orgY = y2
	
	e\list = list2
	
	e\startLength = Len(e\list)
	
	e\worth = 1000
End Function

Function updateEvent() 
	For e.event = Each event
		e\x = lerp(e\x, e\orgX, 0.01)
		e\y = lerp(e\y, e\orgY, 0.01)
		
		If Mid(e\list, 1, 1) = "L" Then
			If KeyHit(203) Then
				e\list = removeChar(e\list, 1)
				splitSprite(e\x-64, e\y-32, Rnd(360), 4, 0, 0, 64, 16)
				e\x = e\orgX - 64
				e\y = e\orgY + Rnd(-SHAKE_MANGNITUDE , SHAKE_MANGNITUDE )
				PlaySound leftSnd
				setColor(Rnd(100, 255), Rnd(100, 255), Rnd(100, 255))
				combo = combo + 1
			End If
			
			If KeyHit(200) Or KeyHit(205) Or KeyHit(208) Then combo = 0
		End If
		
		If Mid(e\list, 1, 1) = "U" Then
			If KeyHit(200) Then
				e\list = removeChar(e\list, 1)
				splitSprite(e\x-64, e\y-32, Rnd(360), 4, 64*3, 0, 64, 16)
				e\x = e\orgX - 64
				e\y = e\orgY + Rnd(-SHAKE_MANGNITUDE , SHAKE_MANGNITUDE )
				PlaySound upSnd
				setColor(Rnd(100, 255), Rnd(100, 255), Rnd(100, 255))
				combo = combo + 1
			End If
			
			If KeyHit(208) Or KeyHit(205) Or KeyHit(203) Then combo = 0
		End If
		
		If Mid(e\list, 1, 1) = "R" Then
			If KeyHit(205) Then
				e\list = removeChar(e\list, 1)
				splitSprite(e\x-64, e\y-32, Rnd(360), 4, 64, 0, 64, 16)
				e\x = e\orgX - 64
				e\y = e\orgY + Rnd(-SHAKE_MANGNITUDE, SHAKE_MANGNITUDE )
				PlaySound rightSnd
				setColor(Rnd(100, 255), Rnd(100, 255), Rnd(100, 255))
				combo = combo + 1
			End If
			
			If KeyHit(200) Or KeyHit(208) Or KeyHit(203) Then combo = 0
		End If
		
		If Mid(e\list, 1, 1) = "D" Then
			If KeyHit(208) Then
				e\list = removeChar(e\list, 1)
				splitSprite(e\x-64, e\y-32, Rnd(360), 4, 64*2, 0, 64, 16)
				e\x = e\orgX - 64
				e\y = e\orgY + Rnd(-64, 64)
				PlaySound downSnd
				setColor(Rnd(100, 255), Rnd(100, 255), Rnd(100, 255))
				combo = combo + 1
			End If
			
			If KeyHit(200) Or KeyHit(205) Or KeyHit(203) Then combo = 0
		End If
		
		e\worth = e\worth * level
		
		If Len(e\list) <= 0 Then 
			e\destroy = 1	
			score = score + e\worth + 10*e\startLength + combo*100
			level = level * 0.9999
			toAddNewEvent = 1
		End If
		
		If e\destroy Then Delete e
	Next
End Function

Function drawEvent() 
	For e.event = Each event
		;Text e\x, e\y, e\list, 1, 1
		
		For i = 1 To Len(e\list) 
			If Mid(e\list, i, 1) = "L" Then
				DrawImageRect(spritesheet, x+i*70+screenShakeX, e\y-32+screenShakeY, 0, 0, 64, 64)
			End If
			If Mid(e\list, i, 1) = "R" Then
				DrawImageRect(spritesheet, x+i*70+screenShakeX, e\y-32+screenShakeY, 64, 0, 64, 64)
			End If
			If Mid(e\list, i, 1) = "D" Then
				DrawImageRect(spritesheet, x+i*70+screenShakeX, e\y-32+screenShakeY, 64*2, 0, 64, 64)
			End If
			If Mid(e\list, i, 1) = "U" Then
				DrawImageRect(spritesheet, x+i*70+screenShakeX, e\y-32+screenShakeY, 64*3, 0, 64, 64)
			End If
		Next
		
		Text e\x-64, e\y+100, "WORTH:" + e\worth
	Next
End Function

Type particle
	Field x#
	Field y#
	
	Field angle#
	Field speed#
	
	Field velX#
	Field velY#
	
	Field fallDown#
	
	Field imx
	Field imy
	
	Field size
	
	Field destroy
End Type

Function addParticle(x2#, y2#, angle2#, speed2#, imx2, imy2, size2)
	p.particle = New particle
	p\x = x2
	p\y = y2
	
	p\angle = angle2
	p\speed = speed2
	
	p\velX = Cos(p\angle) * p\speed
	p\velY = Sin(p\angle) * p\speed
	
	p\imx = imx2
	p\imy = imy2
	
	p\size = size2
End Function

Function updateParticle()
	For p.particle = Each particle
		p\x = p\x + p\velX
		p\y = p\y + p\velY
		
		p\y = p\y + p\fallDown
		
		p\fallDown = p\fallDown + 0.5
		
		If p\y >= winH + p\size Then p\destroy = 1
		
		If p\destroy Then Delete p
	Next
End Function

Function drawParticle()
	For p.particle = Each particle
		DrawImageRect(spritesheet, p\x, p\y, p\imx, p\imy, p\size, p\size)
	Next
End Function

Function splitSprite(x2#, y2#, angle2#, speed2#, imx2, imy2, totalSize, partSize)
	For x = 0 To (totalSize/partSize)-1
		For y = 0 To (totalSize/partSize)-1
			addParticle(x2#+x*partSize, y2#+y*partSize, angle2#+Rnd(360), speed2#+Rnd(-speed2/2, speed2/2), imx2+x*partSize, imy2+y*partSize, partSize)
		Next
	Next
End Function

Global score#
Global displayScore#
Global countDown#
Global maxTime# = 1000
Global toAddNewEvent

Global screenShakeX#
Global screenShakeY#
Global shakeTime

Global startscreen = 1

Global level# = 0.999

Global combo

Global r#, g#, b#

Function setColor(r2#, g2#, b2#)
	r = r2
	g = g2
	b = b2
End Function

Function startGame()
	Local ofile = WriteFile("highscore.txt")
	WriteLine(ofile, Str(highscore))
	CloseFile(ofile)
	
	countDown = 0
	maxTime = 500
	score = 0
	displayScore = 0
	level = 0.999
	toAddNewEvent = 1
	combo = 0
End Function

Function updateGame()
	If countDown <= maxTime Then countDown = countDown + 0.1
	
	If toAddNewEvent Then
		addEvent(winW/2, winH/2, randomList(Rand(10, 15)))
		toAddNewEvent = 0
	End If
	
	r = lerp(r, 0, 0.1)
	g = lerp(g, 0, 0.1)
	b = lerp(b, 0, 0.1)
	
	If shakeTime = 5 Then 
		shakeTime = shakeTime - 1
	End If
	
	If countDown >= maxTime Then
		For e.event = Each event
			e\destroy = 1
		Next
		
		If KeyDown(57) Then
			startGame()
		End If
	End If
End Function

Function drawGame()
	Text 10, 10, "SCORE: " + Int(displayScore)
	Text 10, 30, "TOTAL TIME LEFT: " + Int((maxTime - countDown))
	Color 255, clamp(255-combo*5, 0, 255), clamp(255-combo*5, 0, 255)
	If combo > 0 Then Text 10, 50, "COMBO: " + combo
	Color 255, 255, 255
	
	displayScore = lerp(displayScore, score, 0.1)
	
	If countDown >= maxTime Then
		Text winW/2, winH/2, "GAME OVER!", 1, 1
		Text winW/2, winH/2 + 20, "FINAL SCORE: " + Int(score), 1, 1
		Text winW/2, winH/2 + 40, "HIGHSCORE: " + highscore, 1, 1
	End If
End Function

Function update()
	If startscreen = 0 Then
		updateEvent()
		updateGame()
		updateParticle()
	
		FlushKeys()
	Else 
		If KeyDown(57) Then
			startscreen = 0
		End If
	End If
End Function

Function draw()
	Color r, g, b
	Rect 0, 0, winW, winH
	Color 255, 255, 255

	drawEvent()
	drawGame()
	drawParticle()
	
	If startscreen = 1 Then
		Color 0, 0, 0
		Rect 0, 0, winW, winH
		Color 255, 255, 255
		Color Rand(255), Rand(255), Rand(255)
		Text winW/2, winH/2, "Rhythm Blitz", 1, 1
		Color 255, 255, 255
		Text winW/2, winH/2+20, "PRESS SPACE TO START!", 1, 1
		Text winW/2, winH/2+40, "HIGHSCORE: " + highscore, 1, 1
	End If
End Function 

startGame()

While Not KeyHit(1)
	Cls 
		WaitTimer(frametimer)
		If KeyDown(1) Then End 
		If KeyDown(31) Then 
			spritesheet = LoadImage("stirner.bmp")
			MaskImage(spritesheet, 255, 0, 255)
		End If
		draw()
		update() 
		updateHighscore()
	Flip
Wend