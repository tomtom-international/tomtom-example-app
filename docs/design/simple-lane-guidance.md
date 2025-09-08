# Simple Lane Guidance (SLG)

The implementation of lane guidance in the open source example application deviates from the standard [TomTom App guidelines](https://www.figma.com/design/sdmy74pICyTr9Z6bmuItQj/Next-Instruction-Panel--NIP-?node-id=3677-11898&t=zLFCHFfmSbBnadW8-1). This document outlines the rationale behind these design decisions, focusing on the representation of lanes with multiple directional options.

## Reason for Design Decisions

The One App guidelines do not account for scenarios where a single lane may have multiple directions. To enhance user experience and provide comprehensive lane information, we have chosen to represent all possible directions for a single lane by overlapping icons. This approach is proactive, addressing functionality anticipated in [GOSDK-170493](https://tomtom.atlassian.net/browse/GOSDK-170493).

## Design Implementation

1. **Icon Representation:** All directional icons will be overlapped to display multiple direction options for a single lane. This ensures users are aware of all potential pathways from a single position.

2. **Icon Placement:** Each arrow icon is centered within a box of dimensions specified in the TT App guidelines (`48.dp`). The centering creates a uniform starting point for all directions, enhancing user perception of lane guidance.

3. **Padding Adjustments:** Due to overlapping icons, the padding between lanes will be slightly larger than specified in the original guidelines. This adjustment provides clarity in direction representation while maintaining visual balance.

By opting to overlap icons and adjust paddings, this design choice anticipates future requirements and offers a more comprehensive guidance system for users. These decisions ensure clarity and usability, aligning with overall application goals.
