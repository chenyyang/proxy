
package com.chenyang.proxy;


public class AgentTest {
	static final int SIZE = Integer.parseInt(System.getProperty("size", "1024"));
	static final int CLIENT_SIZE = 500;

    private static boolean isPowerOfTwo(int val) {
        return (val & -val) == val;
    }

	public static void main(String[] args) throws Exception {
        for (int i = 0; i < 1000; i++) {
            if (isPowerOfTwo(i)) {
                System.out.println(i + "  ");

            }
        }
	}
}
