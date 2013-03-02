import os,sys
import Image
import cv
import math

def rgbNormalise(frame):
	
	r=cv.CreateImage(cv.GetSize(frame), 8, 1)
	g=cv.CreateImage(cv.GetSize(frame), 8, 1)
	b=cv.CreateImage(cv.GetSize(frame), 8, 1)

	cv.Split(frame, r,g,b, None) 
	
	rm=cv.GetMat(r)
	gm=cv.GetMat(g)
	bm=cv.GetMat(b)
	msum=cv.GetMat(r)
	
	for i in range(msum.rows):
		for j in range(msum.cols):
			#rm[i,j]=rm[i,j]*rm[i,j]
			#gm[i,j]=gm[i,j]*gm[i,j]
			#bm[i,j]=bm[i,j]*bm[i,j]
			msum[i,j]=rm[i,j]+gm[i,j]+bm[i,j]
			#temp=math.sqrt(float(msum[i,j]))
			rm[i,j]=int(float(rm[i,j])/msum[i,j])
			gm[i,j]=int(float(gm[i,j])/msum[i,j])
			bm[i,j]=int(float(bm[i,j])/msum[i,j])
	cv.Merge(rm,gm,bm, None, frame)
	return frame
