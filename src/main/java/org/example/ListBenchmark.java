package org.example;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.*;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 1)
@Measurement(iterations = 2)
@Fork(1)
public class ListBenchmark {

    @State(Scope.Thread)
    public static class MyState {
        public ArrayList<BigObject> arrayList;
        public LinkedList<BigObject> linkedList;
        public List<BigObject> objectsToInsert;
        ListIterator<BigObject> iterator;

        @Param({"50000"})
        public int n;

        @Setup(Level.Iteration)
        public void setUp() {
            arrayList = new ArrayList<>();
            linkedList = new LinkedList<>();
            objectsToInsert = new ArrayList<>(n/1000);
            Random rand = new Random();

            for (int i = 0; i < n; i++) {
                objectsToInsert.add(new BigObject(rand.nextInt(), generateRandomString(10000)));
            }
        }

        @Setup(Level.Iteration)
        public void setUpForRandomInsertRemove() {
            for (BigObject obj : objectsToInsert) {
                arrayList.add(new BigObject().clone(obj));
                linkedList.add(new BigObject().clone(obj));
            }
        }

        @Setup(Level.Iteration)
        public void setUpForGetMiddlePositionOfLinkedList() {
            iterator = linkedList.listIterator(linkedList.size() / 2);
        }

        @TearDown(Level.Iteration)
        public void tearDown() {
            arrayList.clear();
            linkedList.clear();
            objectsToInsert.clear();
        }

        private String generateRandomString(int length) {
            int leftLimit = 97; // letter 'a'
            int rightLimit = 122; // letter 'z'
            Random random = new Random();
            StringBuilder buffer = new StringBuilder(length);
            for (int i = 0; i < length; i++) {
                int randomLimitedInt = leftLimit + (int)
                        (random.nextFloat() * (rightLimit - leftLimit + 1));
                buffer.append((char) randomLimitedInt);
            }
            return buffer.toString();
        }
    }

    @Benchmark
    public void arrayListInsertAtBeginning(MyState state, Blackhole bh) {
        for (int i = 0; i < state.n; i++) {
            state.arrayList.add(0, new BigObject().clone(state.objectsToInsert.get(i)));
        }
        bh.consume(state.arrayList);
    }

    @Benchmark
    public void linkedListInsertAtBeginning(MyState state, Blackhole bh) {
        for (int i = 0; i < state.n; i++) {
            state.linkedList.add(0, new BigObject().clone(state.objectsToInsert.get(i)));
        }
        bh.consume(state.linkedList);
    }


    @Benchmark
    public void arrayListInsertAtMiddle(MyState state, Blackhole bh) {
        for (int i = 0; i < state.n; i++) {
            int midIndex = state.arrayList.size() / 2;
            state.arrayList.add(midIndex, new BigObject().clone(state.objectsToInsert.get(i)));
        }
        bh.consume(state.arrayList);
    }

    @Benchmark
    public void linkedListInsertAtMiddle(MyState state, Blackhole bh) {
        state.setUpForGetMiddlePositionOfLinkedList();
        int count = 0;
        for (int i = 0; i < state.n; i++) {
            state.iterator.add(new BigObject().clone(state.objectsToInsert.get(i)));
            count++;
            if (count == 2) {
                state.iterator.next();
                count = 0;
            }
        }
        bh.consume(state.linkedList);
    }

    @Benchmark
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void arrayListRemoveFirst(MyState state, Blackhole bh) {
        while (!state.arrayList.isEmpty()) {
            state.arrayList.remove(0);
        }
        bh.consume(state.arrayList);
    }

    @Benchmark
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void linkedListRemoveFirst(MyState state, Blackhole bh) {
        while (!state.linkedList.isEmpty()) {
            state.linkedList.removeFirst();
        }
        bh.consume(state.linkedList);
    }

    @Benchmark
    public void arrayListRandomInsertRemove(MyState state, Blackhole bh) {
        state.setUpForRandomInsertRemove();
        Random rand = new Random();
        for (int i = 0; i < state.n; i++) {
            int index = state.arrayList.isEmpty() ? 0 : rand.nextInt(state.arrayList.size());
            if (rand.nextBoolean() && state.objectsToInsert.size() > 0) {
                state.arrayList.add(index, new BigObject().clone(state.objectsToInsert.get(i)));
            } else if (!state.arrayList.isEmpty()) {
                state.arrayList.remove(index);
            }
        }
        bh.consume(state.arrayList);
    }

    @Benchmark
    public void linkedListRandomInsertRemove(MyState state, Blackhole bh) {
        state.setUpForRandomInsertRemove();
        state.setUpForGetMiddlePositionOfLinkedList();
        Random rand = new Random();
        ListIterator<BigObject> iterator = state.iterator;
        int currentIndex = state.linkedList.size() / 2;
        int lowerBound = state.linkedList.size() / 4;
        int upperBound = state.linkedList.size() * 3 / 4;
        int count = 0;
        for (int i = 0; i < state.n; i++) {
            if (count == 2) {
                state.iterator.next();
                count = 0;
            }
            else if (count == -2) {
                state.iterator.previous();
                count = 0;
            }
            if (currentIndex < lowerBound || currentIndex >= upperBound) {
                iterator = state.iterator;
            }
            if (rand.nextBoolean()) {
                iterator.add(new BigObject().clone(state.objectsToInsert.get(i)));
                currentIndex++;
                count++;
            } else if (iterator.hasNext()) {
                iterator.next();
                iterator.remove();
                currentIndex--;
                count--;
            }
        }
        bh.consume(state.linkedList);
    }
}

