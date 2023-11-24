package com.demo.opencv;

import static org.opencv.imgproc.Imgproc.INTER_CUBIC;
import static org.opencv.imgproc.Imgproc.cvtColor;
import static org.opencv.imgproc.Imgproc.resize;

import android.graphics.Bitmap;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class HashUtil {
    public static String hashCompare( Bitmap Bp1, Bitmap Bp2) {
        //Data definition import section
        Mat src1 = new Mat();
        Mat dst1 = new Mat();
        Mat src2 = new Mat();
        Mat dst2 = new Mat();
        //Read the bitmap to MAT
        Utils.bitmapToMat(Bp1, src1);
        Utils.bitmapToMat(Bp2, src2);
        //Change ARGB to grayscale map, four channels to one channel
        cvtColor(src1, dst1, Imgproc.COLOR_BGR2GRAY);
        cvtColor(src2, dst2, Imgproc.COLOR_BGR2GRAY);
        //Shrink the grayscale chart to 8*8
        resize(dst1, dst1,new Size(8,8) , 0, 0,  INTER_CUBIC);
        resize(dst2, dst2,new Size(8,8) , 0, 0,  INTER_CUBIC);

        //Core algorithm part
        //Here it becomes a two-dimensional array to be obtained with Mat.get(row, cul),
        // two-dimensional because each pixel may have many properties (ARGB) in it.
        // After becoming grayscale, there is only one G, this G is Gray, and the G in front is Green.
        double[][] data1 = new double[64][1];
        double[][] data2 = new double[64][1];
        //iAvg records the average pixel gray value,
        // ARR records the pixel gray value, and data is a springboard.
        int iAvg1 = 0, iAvg2 = 0;
        double[] arr1 = new double[64];
        double[] arr2 = new double[64];
        //Get grayscale to data, use data to recharge arr,
        // calculate the average grayscale value iAvg.
        for (int i = 0; i < 8; i++)
        {
            int tmp = i * 8;
            for (int j = 0; j < 8; j++)
            {
                int tmp1 = tmp + j;
                data1[tmp1] = dst1.get(i,j);
                data2[tmp1] = dst2.get(i,j);
                arr1[tmp1] = data1[tmp1][0];
                arr2[tmp1] = data2[tmp1][0];
                iAvg1 += arr1[tmp1];
                iAvg2 += arr2[tmp1];
            }
        }
        iAvg1 /= 64;
        iAvg2 /= 64;
        //Compare the gray value of each pixel with the average gray value size
        for (int i = 0; i < 64; i++)
        {
            arr1[i] = (arr1[i] >= iAvg1) ? 1 : 0;
            arr2[i] = (arr2[i] >= iAvg2) ? 1 : 0;
        }
        //Calculate the difference value
        int iDiffNum = 0;
        for (int i = 0; i < 64; i++)
            if (arr1[i] != arr2[i])
                ++iDiffNum;
        //output
        if (iDiffNum <= 5)
            return "LIKE";
        else
            return "UNLIKE";
    }
}
