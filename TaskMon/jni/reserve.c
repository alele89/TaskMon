#include "reserve.h"
#include <time.h>
#include <linux/unistd.h>

#define __NR_set_reserve           (__NR_SYSCALL_BASE+379)
#define __NR_cancel_reserve        (__NR_SYSCALL_BASE+380)


int set_reserve(pid_t pid, struct timespec *C, struct timespec *T, int rt_prio) {
  return syscall(__NR_set_reserve, pid, C, T, rt_prio);
}

int cancel_reserve(pid_t pid) {
  return syscall(__NR_cancel_reserve, pid);
}


JNIEXPORT jint JNICALL Java_com_rtes_taskmon_ReserveWrapper_setreserve
      (JNIEnv * je, jclass jc, jint pid, jint c_sec, jlong c_nsec, jint t_sec, jlong t_nsec, jint rt_prio)
    {

	struct timespec C;
	struct timespec T;

	C.tv_sec = c_sec;
	C.tv_nsec = c_nsec;
	T.tv_sec = t_sec;
	T.tv_nsec = t_nsec;


	return set_reserve((pid_t) pid, &C, &T, rt_prio);
}


JNIEXPORT jint JNICALL Java_com_rtes_taskmon_ReserveWrapper_cancelreserve
      (JNIEnv * je, jclass jc, jint pid)
    {

	return cancel_reserve((pid_t) pid);
}


