Operating Systems
Project 3
Sébastien Vaucher

# Q1 & Q2

The count is often wrong.
Solution: synchronize get() and set() in Stock.java

# Q3

Thread.sleep() causes the scheduler to allocate the CPU to the other thread.

# Q7

We need to use notifyAll instead of notify. Otherwise, the thread chosen to wake up might be unable to proceed and
still have the monitor, thus blocking everyone.  