package cw;

import static org.junit.Assert.*;

import org.junit.Test;


public class UserUnit {

	@Test
	public void test() {
		User obj1 = new User(123123, "Luis", "192.168.1.1");
		String address1 = "192.168.1.1";
		String name1 = "Luis";
		int id1 = 123123;
		assertEquals(address1, obj1.getAddress());
		assertEquals(name1, obj1.getName());
		assertEquals(id1, obj1.getId());
	}

}
