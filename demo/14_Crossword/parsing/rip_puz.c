/*******************************************************************************
*   Copyright 2012 Analog Devices Inc.
*
*   Licensed under the Apache License, Version 2.0 (the "License");
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
********************************************************************************/

#include <stdio.h>
#include <stdlib.h>

/* Extract some useful fields from a *.puz file.  For format, see
          http://code.google.com/p/puz/wiki/FileFormat
*/

int main(int argc, char **argv){

  int i,j,k;
  int width, length, num_bytes, buff_length;
  char ch, *file_buffer;
  FILE *fp;

  if (argc<2){
    fprintf(stderr, "Usage: %s file.puz\n",argv[0]);
    exit(1);
  }

  if ( (fp=fopen(argv[1],"r"))==NULL){
    fprintf(stderr, "Couldn't open file %s\n",argv[1]);
    exit(1);
  }

  buff_length=16000; /* Max supported file length in bytes, because
			I'm lazy */

  file_buffer=(char *)malloc(buff_length);
  num_bytes=fread(file_buffer, 1, buff_length, fp);
  if (num_bytes==buff_length){
    fprintf(stderr, "Exceeded maximum allowed file length!\n");
    exit(1);
  }

  width=(int) (file_buffer[0x2C]);
  length=(int) (file_buffer[0x2D]);
  //fprintf(stderr,"width=%d, length=%d\n",width,length);
  
  if (width*length*2+0x34 > num_bytes){
    fprintf(stderr, "Error parsing file!\n");
    exit(1);
  }

  if (0){
    for (i=0;i<width;i++){
      for (j=0;j<length;j++){
	printf("%c",file_buffer[0x34+j+i*length]);
      }
      printf("\n");
    }
  }

  if (1){
    for (i=0;i<width;i++){
      for (j=0;j<length;j++){
	k=file_buffer[0x34+j+i*length];
	if (k=='.'){
	  printf(" -1");
	}
	else{
	  printf(" %2d",k-'A');
	}
      }
      printf("\n");
    }    
  }

  return(0);

}

