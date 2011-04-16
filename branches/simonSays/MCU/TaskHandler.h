#ifndef _TASK_HANDLER_H
#define _TASK_HANDLER_H 1

/**
 * The maximum number of tasks that can be pending
 */
#define TASK_QUEUE_SIZE 20

void TaskHandler_init(void);

void TaskHandler_postTask(void(*funcptr)(void)); 

void TaskHandler_processNextTask(void);

#endif /* _TASK_HANDLER_H */
