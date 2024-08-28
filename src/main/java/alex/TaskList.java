package alex;

import java.util.ArrayList;

import java.io.IOException;

import alex.task.Task;

public class TaskList {
    private ArrayList<Task> list;
    public TaskList(ArrayList<Task> list) {
        this.list = list;
    }

    public int getSize() {
        return this.list.size();
    }
    public void add(Task task, Storage storage) throws IOException {
        this.list.add(task);
        storage.save(list);
    }

    public void delete(int taskNumber, Storage storage, Ui ui) throws IOException {
        Task task = list.get(taskNumber - 1);
        list.remove(taskNumber - 1);
        storage.save(list);
        ui.message("Noted. I've removed this task: ", task, this.list.size());
    }

    public void mark(int taskNumber, Storage storage, Ui ui) throws IOException {
        Task task = list.get(taskNumber - 1);
        task.markAsDone();
        storage.save(list);
        ui.showMark(task);
    }

    public void unmark(int taskNumber, Storage storage, Ui ui) throws IOException {
        Task task = list.get(taskNumber - 1);
        task.markAsUndone();
        storage.save(list);
        ui.showUnmark(task);
    }

    public void showTasks(String line) {
        System.out.println(line + "\nHere are the tasks in your list: ");
        for (int i = 1; i <= this.list.size(); i++) {
            System.out.println(i + "." + this.list.get(i - 1));
        }
        System.out.println(line);
    }
}
