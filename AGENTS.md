# Project Instructions

- Tests must verify real, user-observable behavior rather than only implementation details such as constants, formulas, or helper functions.
- Interaction changes involving windows, pointers, keyboards, focus, hit regions, or platform-native components must verify the complete interaction path using a real platform window and real input events.
- Tests for interaction-region fixes must cover the boundary as well as points inside and outside it, and must assert the final behavioral outcome rather than only coordinate-conversion results.
