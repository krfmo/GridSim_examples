package test01;

import gridsim.GridResource;
import gridsim.GridSim;
import gridsim.Machine;
import gridsim.MachineList;
import gridsim.ResourceCharacteristics;

import java.util.Calendar;
import java.util.LinkedList;

/**����example1����ϰ
 * ����һ����������machine��������Դ
 * �ڴ����κ�GridSimʵ��ǰ���ǵõ���GridSim.Init()*/
public class Test1 {
	/**������*/
	public static void main(String[] args) {
		System.out.println("����һ��������Դ");
		
		try {
			/*��һ������ʼ��GridSim����Ӧ���ڴ����κ�ʵ��֮ǰ���ø÷�����
			 * ���ǲ�����û�г�ʼ��GridSim֮ǰʹ��������Դ�������ᵼ��run-time�쳣*/
			
			int num_user=0;//�û�����Ҫ�������������У���Ϊ����Ҫ�����û�ʵ�壬���Խ�ֵ��Ϊ0
			Calendar calendar=Calendar.getInstance();//�ڲ���ʱ����������¼ģ��Ŀ�ʼ�ͽ���ʱ��
			boolean trace_flag=true;//һ�����Կ��أ�ֵΪ���ʾ��Ҫ���ټ�¼GridSimģ���ÿһ��
			
			//list of files or processing names to be excluded from any statistical measures
			//��ͳ�ƹ����У����������ڵ��ļ����ƺʹ������
			String[] exclude_from_file={""};
			String[] exclude_from_processing={""};
			
			String report_name=null;//�������ƣ���������Ҫд���棬���Բ��������õ�ReportWriter�������
			
			//��ʼ��GridSim��
			System.out.println("��ʼ��GridSim��");
			GridSim.init(num_user, calendar, trace_flag, exclude_from_file,
					exclude_from_processing, report_name);
			
			/*GridSim3.0�Ժ󣬿���ʹ������һ�ֳ�ʼ����ʽ������Ҫ�κ�ͳ�ƹ���
			 * �������£�
			 * GridSim.init(num_user, calendar, trace_flag);*/
			
			/*�ڶ���������һ��������Դ*/
			GridResource gridResource=createGridResource();//
			System.out.println("����1����~");
			
			//NOTE:���ǲ���Ҫ����GridSim.startGridSimulation()
			//��Ϊû���û�ʵ���������Դ��������
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.println("�����ˣ�");
		}
	}
	
	/**
	 * ����һ��������Դ��һ��������Դ����һ������������Machine����
	 * ���Ƶģ�һ����������һ������PE������Ԫ��CPU����
	 * �ڱ����У�����ģ���������Դ��������������ÿ����������һ������PE��*/
	private static GridResource createGridResource(){
		System.out.println("��ʼ��������3��machine��������Դ...");
		
		//�����Ǵ���������Դ���裺
		/*1.����һ�������б����ڴ洢һ����������*/
		MachineList mList=new MachineList();
		System.out.println("����һ�������б�");
		
		/*2.����һ�������������ֱ��ǣ�����id��PE������MIPS rating���������ļ���������
		 * �����У���ʹ����Դ����ϢΪ��hpc420.hpcc.jp, AIST, Tokyo, Japan
		 * NOTE��these data are taken the from GridSim paper, page 25.
		 * �����У�����PE������ͬ��MIPS(Millions Instruction Per Second) Rating
		 * ����ÿ��PE����������ͬ
         * */
		int mipsRating=377;
		mList.add(new Machine(0, 4, mipsRating));//��һ̨����
		System.out.println("������һ̨����������������Ϊ4���ѽ����������б�");
		
		/*3.�����Ҫ��������������ظ��ڶ�����
		 * �ڱ����У��ձ���AIST��3��������ÿ��������MIPS����ͬ��PE������ͬ
		 * NOTE�������ֻ��ҪΪÿһ��������Դ����һ����������ô����ʡ���ⲽ
		 * */
		mList.add(new Machine(1, 4, mipsRating));//�ڶ�̨����
		System.out.println("�����ڶ�̨����������������Ϊ4���ѽ����������б�");
		
		mList.add(new Machine(2, 2, mipsRating));//����̨����
		System.out.println("��������̨����������������Ϊ2���ѽ����������б�");
		
		/*4.����һ����Դ���Զ��������洢������Դ���ԣ�
		 * ϵͳ��ϵ�ṹ������ϵͳ�������б�������ԣ�ʱ��/�ռ乲��ʱ���ʹ��ۣ�G$/PE time unit��
		 * */
		String arch="Sun Ultra";//ϵͳ��ϵ�ṹ
		String os="Solaris";//����ϵͳ
		double time_zone=9.0;//��Դ����ʱ��
		double cost=3.0;//ʹ�ø���Դ�Ĵ���
		
		ResourceCharacteristics resConfig=new ResourceCharacteristics(arch, os, mList, ResourceCharacteristics.TIME_SHARED, time_zone, cost);
		System.out.println();
		System.out.println("������������Դ���Զ��󣬲��洢�˻����б�");
		
		//5.���գ�������Ҫ����һ���������
		String name="Resource_0";//��Դ����
		double baud_rate=100.0;//ͨ���ٶȣ���������
		long seed=11L*13*17*19*23+1;//???
		double peakLoad=0.0;//�߷�ʱ����Դ����
		double offPeakLoad=0.0;//�Ǹ߷�ʱ����Դ����
		double holidayLoad=0.0;//����ʱ����Դ����
		
		//�൱������ĩ��������Դһ�ܹ���7��
		LinkedList<Integer> Weekends=new LinkedList<>();
		Weekends.add(new Integer(Calendar.SATURDAY));
		Weekends.add(new Integer(Calendar.SUNDAY));
		
		//�޼��ա�Ȼ���������в�û�����ü���
		LinkedList<Integer> Holidays=new LinkedList<>();
		
		GridResource gridRes=null;
		try {
			gridRes=new GridResource(name, baud_rate, seed,
					resConfig, peakLoad, offPeakLoad, holidayLoad, Weekends, Holidays);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
		System.out.println("��󣬴���һ��������Դ���洢�˸�������Դ������");
		
		return gridRes;
	}
}





































