/**
 * Author: ly
 * Date: March 2016
 */

������һ�������������java����
�������������У����ǲ���ġ����������ڰٶ����ҵ���eclipse���иó���ķ���
�Ҽ�Test.java��Run As -> Run Configurations
�ҵ���x��=Arguments������������������ɣ�����֮���ÿո����

To run the class file:
    In Unix/Linux:
        java -cp $GRIDSIM/jars/gridsim.jar:. Test [space | time] [test num]

    In Windows:
        java -cp %GRIDSIM%\jars\gridsim.jar;. Test [space | time] [test num]

For example:
    In Unix/Linux to run a SpaceShared algorithm for TestCase #8:
        java -classpath $GRIDSIM/jars/gridsim.jar:. Test space 8 > outputSpaceTest8.txt

