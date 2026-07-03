// pnpm install framer-motion ...
// 
import { motion } from 'framer-motion';

export default function AnimatedCard({ children,  

    } : { children: React.ReactNode; }) {
        return (
            <motion.div
                initial={ { opacity: 0, y: 20 }}
                animate={ { opacity: 1, y: 0 }}
                whileHover={{ y: -6,scale: 1.05 }}
                // exit={exit || { opacity: 0, y: -20 }}
                transition={{ duration: 0.5 }}
            >
            {children}
        </motion.div>
    );
}