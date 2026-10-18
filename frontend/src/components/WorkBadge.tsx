import Chip from "@mui/material/Chip";
import RadioButtonUncheckedIcon from "@mui/icons-material/RadioButtonUnchecked";
import TimelapseIcon from "@mui/icons-material/Timelapse";
import RateReviewOutlinedIcon from "@mui/icons-material/RateReviewOutlined";
import TaskAltIcon from "@mui/icons-material/TaskAlt";
import BlockOutlinedIcon from "@mui/icons-material/BlockOutlined";
import CancelOutlinedIcon from "@mui/icons-material/CancelOutlined";
import KeyboardDoubleArrowDownIcon from "@mui/icons-material/KeyboardDoubleArrowDown";
import DragHandleIcon from "@mui/icons-material/DragHandle";
import KeyboardDoubleArrowUpIcon from "@mui/icons-material/KeyboardDoubleArrowUp";
import PriorityHighIcon from "@mui/icons-material/PriorityHigh";
import AssignmentOutlinedIcon from "@mui/icons-material/AssignmentOutlined";
import BugReportOutlinedIcon from "@mui/icons-material/BugReportOutlined";
import AutoAwesomeOutlinedIcon from "@mui/icons-material/AutoAwesomeOutlined";
import TrendingUpIcon from "@mui/icons-material/TrendingUp";
import LayersOutlinedIcon from "@mui/icons-material/LayersOutlined";
import PlayCircleOutlineIcon from "@mui/icons-material/PlayCircleOutlined";
import PauseCircleOutlineIcon from "@mui/icons-material/PauseCircleOutlined";
import Inventory2OutlinedIcon from "@mui/icons-material/Inventory2Outlined";
import LockOutlinedIcon from "@mui/icons-material/LockOutlined";
import GroupsOutlinedIcon from "@mui/icons-material/GroupsOutlined";
import BusinessOutlinedIcon from "@mui/icons-material/BusinessOutlined";
import PublicIcon from "@mui/icons-material/Public";
import { TaskPriority, TaskStatus, TaskType } from "../features/tasks/types/task.types";
import { ProjectStatus, ProjectVisibility } from "../features/projects/types/project.types";

// Soft brand shades and urgency accents keep every label readable on its own background.
const tones = {
  slate: { color: "#46576B", backgroundColor: "#EFF3F7", borderColor: "#D6DFE8" },
  blue: { color: "#0054A6", backgroundColor: "#EAF3FF", borderColor: "#BDD9FA" },
  cyan: { color: "#00617A", backgroundColor: "#E5F6FB", borderColor: "#ADE0EC" },
  teal: { color: "#00665E", backgroundColor: "#E2F5F1", borderColor: "#ABDCD1" },
  amber: { color: "#855000", backgroundColor: "#FFF4D6", borderColor: "#EED18A" },
  orange: { color: "#A64708", backgroundColor: "#FFF0E3", borderColor: "#F1C4A1" },
  red: { color: "#A32E36", backgroundColor: "#FDEBED", borderColor: "#F1BCC4" },
  navy: { color: "#143552", backgroundColor: "#EAF0F8", borderColor: "#C5D4E5" },
} as const;

type Definition = { tone: keyof typeof tones; icon: typeof TaskAltIcon };

const definitions = {
  status: {
    TODO: { tone: "slate", icon: RadioButtonUncheckedIcon },
    IN_PROGRESS: { tone: "blue", icon: TimelapseIcon },
    IN_REVIEW: { tone: "amber", icon: RateReviewOutlinedIcon },
    BLOCKED: { tone: "red", icon: BlockOutlinedIcon },
    COMPLETED: { tone: "teal", icon: TaskAltIcon },
    CANCELLED: { tone: "slate", icon: CancelOutlinedIcon },
  } satisfies Record<TaskStatus, Definition>,
  priority: {
    LOW: { tone: "teal", icon: KeyboardDoubleArrowDownIcon },
    MEDIUM: { tone: "amber", icon: DragHandleIcon },
    HIGH: { tone: "orange", icon: KeyboardDoubleArrowUpIcon },
    CRITICAL: { tone: "red", icon: PriorityHighIcon },
  } satisfies Record<TaskPriority, Definition>,
  type: {
    TASK: { tone: "cyan", icon: AssignmentOutlinedIcon },
    BUG: { tone: "red", icon: BugReportOutlinedIcon },
    FEATURE: { tone: "blue", icon: AutoAwesomeOutlinedIcon },
    IMPROVEMENT: { tone: "teal", icon: TrendingUpIcon },
    EPIC: { tone: "navy", icon: LayersOutlinedIcon },
  } satisfies Record<TaskType, Definition>,
  projectStatus: {
    PLANNING: { tone: "blue", icon: RadioButtonUncheckedIcon },
    ACTIVE: { tone: "cyan", icon: PlayCircleOutlineIcon },
    ON_HOLD: { tone: "amber", icon: PauseCircleOutlineIcon },
    COMPLETED: { tone: "teal", icon: TaskAltIcon },
    ARCHIVED: { tone: "slate", icon: Inventory2OutlinedIcon },
  } satisfies Record<ProjectStatus, Definition>,
  visibility: {
    PRIVATE: { tone: "slate", icon: LockOutlinedIcon },
    TEAM: { tone: "blue", icon: GroupsOutlinedIcon },
    ORGANIZATION: { tone: "teal", icon: BusinessOutlinedIcon },
    PUBLIC: { tone: "cyan", icon: PublicIcon },
  } satisfies Record<ProjectVisibility, Definition>,
};

type WorkBadgeProps =
  | { kind: "status"; value: TaskStatus }
  | { kind: "priority"; value: TaskPriority }
  | { kind: "type"; value: TaskType }
  | { kind: "projectStatus"; value: ProjectStatus }
  | { kind: "visibility"; value: ProjectVisibility };

/** Use the same semantics in lists and selects; text and icons supplement the color. */
export default function WorkBadge({ kind, value }: WorkBadgeProps) {
  const definition = (definitions[kind] as Record<string, Definition>)[value];
  const { icon: Icon, tone } = definition ?? { icon: AssignmentOutlinedIcon, tone: "slate" };
  return <Chip component="span" size="small" icon={<Icon />} label={value.replaceAll("_", " ")}
    sx={{
      height: 26, border: "1px solid", ...tones[tone], borderRadius: "999px", flexShrink: 0,
      fontSize: "0.75rem", fontWeight: 650, letterSpacing: "0.02em", verticalAlign: "middle",
      "& .MuiChip-icon": { color: "inherit", fontSize: 16, ml: 0.75, mr: -0.25 },
      "& .MuiChip-label": { px: 1 },
    }} />;
}
