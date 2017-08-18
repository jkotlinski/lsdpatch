package fontEditor;

	public abstract class ChangeEventListener {
		public enum ChangeEventMouseSide {
			LEFT,
			RIGHT
		};
		/**
		 * 
		 * @param color
		 * @param side 
		 */
		public abstract void onChange(int color, ChangeEventMouseSide side);
	}

